package meetingatleti;

import java.util.ArrayList;
import javax.swing.*;

/**
 * Form 1 di 3 – Creazione e iscrizione atleti.
 *
 * Logica del form:
 *   Se AppData.garaCorrente != null:
 *     → BTN_Aggiungi tenta l'iscrizione alla gara con (Atleta, Prestazione).
 *       Se fallisce, offre di salvare l'atleta come LIBERO (con prestazione in attesa).
 *   Se AppData.garaCorrente == null:
 *     → L'atleta + Prestazione vengono salvati in AppData come LIBERI.
 *       Potranno essere aggiunti a una gara tramite FRM_Gara (tasto destro).
 *
 * v3: costruisciAtleta() e costruisciPrestazione() sono separati.
 *     L'iscrizione usa sempre gara.iscrizione(atleta, prestazione).
 */
public class FRM_Atleti extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(FRM_Atleti.class.getName());

    private final DefaultListModel<String> modelAtleti  = new DefaultListModel<>();
    private final ArrayList<Atleta>        indiceAtleti = new ArrayList<>();

    public FRM_Atleti() {
        initComponents();

        CMB_TipoAtleta.setModel(new DefaultComboBoxModel<>(
                new String[]{"Velocista", "Fondometrista", "Ostacolista", "Saltatore", "Pesista"}));
        CMB_TipoAtleta.addActionListener(e -> aggiornaCampiExtra());

        LST_Atleti.setModel(modelAtleti);
        LST_Atleti.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        RBT_M.setSelected(true);

        aggiornaInfoGara();
        aggiornaCampiExtra();
        aggiornaLista();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  LOGICA APPLICATIVA
    // ══════════════════════════════════════════════════════════════════════

    private void aggiornaInfoGara() {
        Gara gara = AppData.getInstance().getGaraCorrente();
        if (gara != null) {
            LBL_InfoGara.setText("Gara attiva:  " + gara.getNomeGara()
                    + "  [" + gara.getCategoria() + "]  –  " + gara.getTipoDescrizione());
            LBL_InfoGara.setForeground(new java.awt.Color(0, 100, 0));
        } else {
            LBL_InfoGara.setText("⚠  Nessuna gara selezionata  –  l'atleta sarà salvato come ATLETA LIBERO");
            LBL_InfoGara.setForeground(new java.awt.Color(180, 80, 0));
        }
    }

    private void aggiornaLista() {
        modelAtleti.clear();
        indiceAtleti.clear();

        for (Atleta a : AppData.getInstance().getAtletiLiberi()) {
            modelAtleti.addElement("[LIBERO] " + a.toString());
            indiceAtleti.add(a);
        }

        Gara gara = AppData.getInstance().getGaraCorrente();
        if (gara != null) {
            AppData.getInstance().setGaraCorrente(gara);   // assicura contesto per toString
            for (Atleta a : gara.getAtleti()) {
                modelAtleti.addElement(a.toString());
                indiceAtleti.add(a);
            }
        }
    }

    private void aggiornaCampiExtra() {
        String tipo = (String) CMB_TipoAtleta.getSelectedItem();
        if (tipo == null) return;

        LBL_Stat2.setVisible(false);
        TXT_Stat2.setVisible(false);

        switch (tipo) {
            case "Velocista":
                LBL_Stat1.setText("Tempo gara (sec):");
                LBL_Stat2.setText("Tempo reazione (sec, > 0.5):");
                LBL_Stat2.setVisible(true);
                TXT_Stat2.setVisible(true);
                break;
            case "Ostacolista":
                LBL_Stat1.setText("Tempo gara (sec):");
                LBL_Stat2.setText("Penalità ostacoli (cs):");
                LBL_Stat2.setVisible(true);
                TXT_Stat2.setVisible(true);
                break;
            case "Fondometrista":
                LBL_Stat1.setText("Tempo gara (sec):"); break;
            case "Saltatore":
                LBL_Stat1.setText("Distanza salto (cm):"); break;
            case "Pesista":
                LBL_Stat1.setText("Distanza lancio (cm):"); break;
        }

        TXT_Stat1.setText("");
        TXT_Stat2.setText("");
        revalidate();
        repaint();
    }

    private void aggiungiAtleta() {
        // ── validazione base ───────────────────────────────────────────────
        String nome    = TXT_Nome.getText().trim();
        String sesso   = RBT_M.isSelected() ? "M" : "F";
        String etaStr  = TXT_Eta.getText().trim();
        String pettStr = TXT_Pettorale.getText().trim();
        String tipo    = (String) CMB_TipoAtleta.getSelectedItem();

        if (nome.isEmpty() || etaStr.isEmpty() || pettStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Compila tutti i campi obbligatori: Nome, Età, Pettorale.",
                    "Dati incompleti", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int eta, pettorale;
        try {
            eta       = Integer.parseInt(etaStr);
            pettorale = Integer.parseInt(pettStr);
            if (eta <= 0 || pettorale <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Età e Pettorale devono essere numeri interi positivi.",
                    "Errore input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ── costruzione atleta e prestazione ──────────────────────────────
        Atleta atleta;
        Prestazione prestazione;
        try {
            atleta      = costruisciAtleta(tipo, nome, sesso, eta, pettorale);
            prestazione = costruisciPrestazione(tipo);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Errore statistiche", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ── iscrizione o atleta libero ────────────────────────────────────
        Gara gara = AppData.getInstance().getGaraCorrente();

        if (gara != null) {
            boolean ok = gara.iscrizione(atleta, prestazione);
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        "✔  Atleta aggiunto alla gara con successo!\n" + atleta,
                        "Iscrizione OK", JOptionPane.INFORMATION_MESSAGE);
            } else {
                String motivoTipo = "";
                if (gara.getTipoGaraCorsa()  != null && !(atleta instanceof Velocisti))
                    motivoTipo = "• Gara di CORSA: accetta solo Velocista / Fondometrista / Ostacolista\n";
                else if (gara.getTipoGaraSalto()  != null && !(atleta instanceof Saltatori))
                    motivoTipo = "• Gara di SALTO: accetta solo Saltatore\n";
                else if (gara.getTipoGaraLancio() != null && !(atleta instanceof Lanciatori))
                    motivoTipo = "• Gara di LANCIO: accetta solo Pesista\n";

                int risposta = JOptionPane.showConfirmDialog(this,
                        "Impossibile iscrivere l'atleta alla gara:\n"
                        + motivoTipo
                        + "• Pettorale " + pettorale + " già in uso, oppure\n"
                        + "• Sesso '" + sesso + "' ≠ categoria '" + gara.getCategoria() + "'\n\n"
                        + "Salvarlo come ATLETA LIBERO (potrà essere aggiunto in seguito)?",
                        "Iscrizione fallita", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                if (risposta == JOptionPane.YES_OPTION) {
                    AppData.getInstance().aggiungiAtletaLibero(atleta, prestazione);
                    JOptionPane.showMessageDialog(this,
                            "✔  Atleta salvato come LIBERO.\n"
                            + "Aggiungilo a una gara da FRM_Gara (tasto destro → Aggiungi a gara).",
                            "Atleta libero", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    return;
                }
            }
        } else {
            // Nessuna gara attiva → libero con prestazione in attesa
            AppData.getInstance().aggiungiAtletaLibero(atleta, prestazione);
            JOptionPane.showMessageDialog(this,
                    "✔  Atleta salvato come ATLETA LIBERO.\n"
                    + "Seleziona una gara in FRM_Gara e aggiungilo con tasto destro.",
                    "Atleta libero", JOptionPane.INFORMATION_MESSAGE);
        }

        pulisciCampi();
        aggiornaLista();
    }

    // ── costruisciAtleta ──────────────────────────────────────────────────

    /**
     * Crea l'oggetto Atleta corretto (senza prestazione).
     * La prestazione è creata separatamente da costruisciPrestazione().
     */
    private Atleta costruisciAtleta(String tipo, String nome, String sesso,
                                    int eta, int pettorale)
            throws IllegalArgumentException {
        switch (tipo) {
            case "Velocista":
            case "Ostacolista":
            case "Fondometrista":
                return new Velocisti(nome, sesso, eta, pettorale);
            case "Saltatore":
                return new Saltatori(nome, sesso, eta, pettorale);
            case "Pesista":
                return new Lanciatori(nome, sesso, eta, pettorale);
            default:
                throw new IllegalArgumentException("Tipo atleta non riconosciuto: " + tipo);
        }
    }

    /**
     * Crea la Prestazione in base al tipo selezionato e ai valori nei campi.
     * Lancia IllegalArgumentException se i valori non sono validi.
     */
    private Prestazione costruisciPrestazione(String tipo)
            throws IllegalArgumentException {
        switch (tipo) {
            case "Velocista": {
                double tempo, reazioneSec;
                try {
                    tempo       = Double.parseDouble(TXT_Stat1.getText().trim());
                    reazioneSec = Double.parseDouble(TXT_Stat2.getText().trim());
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException(
                            "Tempo gara (sec) e Tempo reazione (sec) devono essere numeri validi.");
                }
                if (reazioneSec <= 0.5) throw new IllegalArgumentException(
                        "Il tempo di reazione deve essere maggiore di 0.5 secondi.\n"
                        + "Valore inserito: " + reazioneSec + " sec");
                return Prestazione.velocista(tempo, (int)(reazioneSec * 100));
            }
            case "Ostacolista": {
                double tempo;
                int    penalita;
                try {
                    tempo    = Double.parseDouble(TXT_Stat1.getText().trim());
                    penalita = Integer.parseInt(TXT_Stat2.getText().trim());
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException(
                            "Tempo gara (sec) e Penalità ostacoli (cs) devono essere numeri validi.");
                }
                return Prestazione.ostacolista(tempo, penalita);
            }
            case "Fondometrista": {
                double tempo;
                try {
                    tempo = Double.parseDouble(TXT_Stat1.getText().trim());
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException(
                            "Tempo gara deve essere un numero decimale (es. 210.5).");
                }
                return Prestazione.fondometrista(tempo);
            }
            case "Saltatore": {
                int distanza;
                try {
                    distanza = Integer.parseInt(TXT_Stat1.getText().trim());
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException(
                            "Distanza salto deve essere un numero intero (cm).");
                }
                return Prestazione.salto(distanza);
            }
            case "Pesista": {
                int distanza;
                try {
                    distanza = Integer.parseInt(TXT_Stat1.getText().trim());
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException(
                            "Distanza lancio deve essere un numero intero (cm).");
                }
                return Prestazione.lancio(distanza);
            }
            default:
                throw new IllegalArgumentException("Tipo atleta non riconosciuto: " + tipo);
        }
    }

    private void pulisciCampi() {
        TXT_Nome.setText("");
        TXT_Eta.setText("");
        TXT_Pettorale.setText("");
        TXT_Stat1.setText("");
        TXT_Stat2.setText("");
        RBT_M.setSelected(true);
    }

    // ── Navigazione ────────────────────────────────────────────────────────

    private void goSinistra() { new FRM_Gara().setVisible(true); this.dispose(); }
    private void goDestra()   { new FRM_Classifica().setVisible(true); this.dispose(); }

    // ══════════════════════════════════════════════════════════════════════
    //  INIT COMPONENTS  (layout identico all'originale)
    // ══════════════════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private void initComponents() {

        ButtonGroup grpSesso = new ButtonGroup();

        LBL_InfoGara    = new javax.swing.JLabel();
        LBL_Nome        = new javax.swing.JLabel();
        TXT_Nome        = new javax.swing.JTextField();
        LBL_Sesso       = new javax.swing.JLabel();
        RBT_M           = new javax.swing.JRadioButton();
        RBT_F           = new javax.swing.JRadioButton();
        LBL_Eta         = new javax.swing.JLabel();
        TXT_Eta         = new javax.swing.JTextField();
        LBL_Pettorale   = new javax.swing.JLabel();
        TXT_Pettorale   = new javax.swing.JTextField();
        LBL_Tipo        = new javax.swing.JLabel();
        CMB_TipoAtleta  = new javax.swing.JComboBox<>();
        LBL_Stat1       = new javax.swing.JLabel();
        TXT_Stat1       = new javax.swing.JTextField();
        LBL_Stat2       = new javax.swing.JLabel();
        TXT_Stat2       = new javax.swing.JTextField();
        BTN_Aggiungi    = new javax.swing.JButton();
        jSeparator1     = new javax.swing.JSeparator();
        jScrollPane1    = new javax.swing.JScrollPane();
        LST_Atleti      = new javax.swing.JList<>();
        BTN_Sinistra    = new javax.swing.JButton();
        BTN_Destra      = new javax.swing.JButton();
        LBL_Form        = new javax.swing.JLabel();
        LBL_ListaTitolo = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Inserimento Atleti  [1 / 3]");

        LBL_InfoGara.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
        LBL_InfoGara.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LBL_InfoGara.setText("...");

        LBL_Nome.setText("Nome:");
        TXT_Nome.setColumns(18);

        LBL_Sesso.setText("Sesso:");
        grpSesso.add(RBT_M); RBT_M.setText("M");
        grpSesso.add(RBT_F); RBT_F.setText("F");

        LBL_Eta.setText("Età:");
        TXT_Eta.setColumns(5);

        LBL_Pettorale.setText("Pettorale:");
        TXT_Pettorale.setColumns(5);

        LBL_Tipo.setText("Tipo atleta:");
        LBL_Stat1.setText("Statistica 1:");
        TXT_Stat1.setColumns(10);
        LBL_Stat2.setText("Statistica 2:");
        TXT_Stat2.setColumns(10);

        BTN_Aggiungi.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 13));
        BTN_Aggiungi.setText("➕  Aggiungi atleta");
        BTN_Aggiungi.setToolTipText("Iscrive l'atleta alla gara oppure lo salva come Libero");
        BTN_Aggiungi.addActionListener(e -> aggiungiAtleta());

        jSeparator1.setOrientation(javax.swing.SwingConstants.HORIZONTAL);

        LBL_ListaTitolo.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 11));
        LBL_ListaTitolo.setText("Atleti inseriti (gara corrente + liberi):");
        LST_Atleti.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 11));
        jScrollPane1.setViewportView(LST_Atleti);

        BTN_Sinistra.setText("<");
        BTN_Sinistra.setToolTipText("Torna a Gestione Gare (Form 0)");
        BTN_Sinistra.addActionListener(e -> goSinistra());

        BTN_Destra.setText(">");
        BTN_Destra.setToolTipText("Vai a Classifica Gara (Form 2)");
        BTN_Destra.addActionListener(e -> goDestra());

        LBL_Form.setText("1 / 3");
        LBL_Form.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);

        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(LBL_InfoGara, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(LBL_Nome, 80, 80, 80).addGap(6)
                        .addComponent(TXT_Nome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(LBL_Sesso, 80, 80, 80).addGap(6)
                        .addComponent(RBT_M).addGap(10).addComponent(RBT_F))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(LBL_Eta, 80, 80, 80).addGap(6)
                        .addComponent(TXT_Eta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20).addComponent(LBL_Pettorale).addGap(6)
                        .addComponent(TXT_Pettorale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(LBL_Tipo, 80, 80, 80).addGap(6)
                        .addComponent(CMB_TipoAtleta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(LBL_Stat1, 130, 130, 130).addGap(6)
                        .addComponent(TXT_Stat1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(LBL_Stat2, 130, 130, 130).addGap(6)
                        .addComponent(TXT_Stat2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(BTN_Aggiungi)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
                    .addComponent(LBL_ListaTitolo)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(BTN_Sinistra)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(LBL_Form, 40, 40, 40)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(BTN_Destra)))
                .addContainerGap(20, 20))
        );

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(12, 12)
                .addComponent(LBL_InfoGara).addGap(14)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(LBL_Nome)
                    .addComponent(TXT_Nome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(LBL_Sesso).addComponent(RBT_M).addComponent(RBT_F))
                .addGap(8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(LBL_Eta)
                    .addComponent(TXT_Eta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(LBL_Pettorale)
                    .addComponent(TXT_Pettorale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(LBL_Tipo)
                    .addComponent(CMB_TipoAtleta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(LBL_Stat1)
                    .addComponent(TXT_Stat1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(LBL_Stat2)
                    .addComponent(TXT_Stat2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(14)
                .addComponent(BTN_Aggiungi).addGap(14)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 6, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8)
                .addComponent(LBL_ListaTitolo).addGap(6)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
                .addGap(10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(BTN_Sinistra).addComponent(LBL_Form).addComponent(BTN_Destra))
                .addContainerGap(12, 12))
        );

        pack();
        setMinimumSize(new java.awt.Dimension(560, 560));
        setLocationRelativeTo(null);
    }

    // ── variabili ─────────────────────────────────────────────────────────
    private javax.swing.JButton           BTN_Aggiungi;
    private javax.swing.JButton           BTN_Sinistra;
    private javax.swing.JButton           BTN_Destra;
    private javax.swing.JComboBox<String> CMB_TipoAtleta;
    private javax.swing.JLabel            LBL_InfoGara;
    private javax.swing.JLabel            LBL_Nome;
    private javax.swing.JLabel            LBL_Sesso;
    private javax.swing.JLabel            LBL_Eta;
    private javax.swing.JLabel            LBL_Pettorale;
    private javax.swing.JLabel            LBL_Tipo;
    private javax.swing.JLabel            LBL_Stat1;
    private javax.swing.JLabel            LBL_Stat2;
    private javax.swing.JLabel            LBL_Form;
    private javax.swing.JLabel            LBL_ListaTitolo;
    private javax.swing.JRadioButton      RBT_M;
    private javax.swing.JRadioButton      RBT_F;
    private javax.swing.JTextField        TXT_Nome;
    private javax.swing.JTextField        TXT_Eta;
    private javax.swing.JTextField        TXT_Pettorale;
    private javax.swing.JTextField        TXT_Stat1;
    private javax.swing.JTextField        TXT_Stat2;
    private javax.swing.JList<String>     LST_Atleti;
    private javax.swing.JScrollPane       jScrollPane1;
    private javax.swing.JSeparator        jSeparator1;
}
