package meetingatleti;

import java.util.ArrayList;
import javax.swing.*;

/**
 * Form 1 di 3 – Creazione e iscrizione atleti.
 *
 * ═══════════════════════════════════════════════════════════
 *  LOGICA DEL FORM:
 *
 *  Se AppData.garaCorrente != null:
 *    → BTN_Aggiungi tenta l'iscrizione alla gara.
 *      Se fallisce (tipo/sesso/pettorale errato), offre
 *      di salvare l'atleta come LIBERO.
 *
 *  Se AppData.garaCorrente == null:
 *    → L'atleta viene salvato direttamente in atletiLiberi.
 *      Potrà essere aggiunto a una gara tramite il menu
 *      contestuale (tasto destro) in FRM_Gara.
 *
 *  CMB_TipoAtleta: Velocista / Fondometrista / Ostacolista /
 *                  Saltatore / Pesista
 *    → mostra/nasconde i campi statistici opportuni.
 *
 *  Navigazione:
 *    BTN_Sinistra  →  torna a FRM_Gara   (Form 0)
 *    BTN_Destra    →  va a FRM_Classifica (Form 2)
 * ═══════════════════════════════════════════════════════════
 */
public class FRM_Atleti extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(FRM_Atleti.class.getName());

    private final DefaultListModel<String> modelAtleti = new DefaultListModel<>();
    private final ArrayList<Atleta>        indiceAtleti = new ArrayList<>();

    // ────────────────────────────────────────────────────────────────────────

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

    /** Mostra in LBL_InfoGara la gara attiva (o avvisa che non ce n'è una). */
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

    /** Ricarica LST_Atleti con atleti liberi + atleti della gara corrente. */
    private void aggiornaLista() {
        modelAtleti.clear();
        indiceAtleti.clear();

        for (Atleta a : AppData.getInstance().getAtletiLiberi()) {
            modelAtleti.addElement("[LIBERO] " + a.toString());
            indiceAtleti.add(a);
        }

        Gara gara = AppData.getInstance().getGaraCorrente();
        if (gara != null) {
            for (Atleta a : gara.getAtleti()) {
                modelAtleti.addElement(a.toString());
                indiceAtleti.add(a);
            }
        }
    }

    /**
     * Mostra/nasconde i campi statistici in base al tipo di atleta scelto.
     *
     *   Velocista    → TXT_Stat1 = tempoGara (sec)   | TXT_Stat2 = tempoReazione (cs)
     *   Fondometrista→ TXT_Stat1 = tempoGara (sec)   | TXT_Stat2 nascosto
     *   Ostacolista  → TXT_Stat1 = tempoGara (sec)   | TXT_Stat2 = penalità ostacoli (cs)
     *   Saltatore    → TXT_Stat1 = distanzaSalto (cm)| TXT_Stat2 nascosto
     *   Pesista      → TXT_Stat1 = distanzaLancio(cm)| TXT_Stat2 nascosto
     */
    private void aggiornaCampiExtra() {
        String tipo = (String) CMB_TipoAtleta.getSelectedItem();
        if (tipo == null) return;

        // reset visibilità
        LBL_Stat2.setVisible(false);
        TXT_Stat2.setVisible(false);
        TXT_Stat1.setEnabled(true);

        switch (tipo) {
            case "Velocista":
                LBL_Stat1.setText("Tempo gara (sec):");
                LBL_Stat2.setText("Tempo reazione (cs):");
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
                LBL_Stat1.setText("Tempo gara (sec):");
                break;
            case "Saltatore":
                LBL_Stat1.setText("Distanza salto (cm):");
                break;
            case "Pesista":
                LBL_Stat1.setText("Distanza lancio (cm):");
                break;
        }

        TXT_Stat1.setText("");
        TXT_Stat2.setText("");
        revalidate();
        repaint();
    }

    /** Legge i campi, crea l'atleta e lo iscrive alla gara (o lo salva come libero). */
    private void aggiungiAtleta() {
        // ── validazione base ──────────────────────────────────────────────
        String nome        = TXT_Nome.getText().trim();
        String sesso       = RBT_M.isSelected() ? "M" : "F";
        String etaStr      = TXT_Eta.getText().trim();
        String pettStr     = TXT_Pettorale.getText().trim();
        String tipo        = (String) CMB_TipoAtleta.getSelectedItem();

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

        // ── costruzione atleta per tipo ───────────────────────────────────
        Atleta atleta;
        try {
            atleta = costruisciAtleta(tipo, nome, sesso, eta, pettorale);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Errore statistiche", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ── iscrizione o atleta libero ────────────────────────────────────
        Gara gara = AppData.getInstance().getGaraCorrente();

        if (gara != null) {
            boolean ok = gara.iscrizione(atleta);
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        "✔  Atleta aggiunto alla gara con successo!\n" + atleta,
                        "Iscrizione OK", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // iscrizione fallita → offri di salvarlo come libero
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
                    AppData.getInstance().getAtletiLiberi().add(atleta);
                    JOptionPane.showMessageDialog(this,
                            "✔  Atleta salvato come LIBERO.\n"
                            + "Aggiungilo a una gara da FRM_Gara (tasto destro → Aggiungi a gara).",
                            "Atleta libero", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    return; // annullato
                }
            }
        } else {
            // Nessuna gara attiva → libero direttamente
            AppData.getInstance().getAtletiLiberi().add(atleta);
            JOptionPane.showMessageDialog(this,
                    "✔  Atleta salvato come ATLETA LIBERO.\n"
                    + "Seleziona una gara in FRM_Gara e aggiungilo con tasto destro.",
                    "Atleta libero", JOptionPane.INFORMATION_MESSAGE);
        }

        pulisciCampi();
        aggiornaLista();
    }

    /**
     * Crea l'oggetto Atleta corretto in base al tipo selezionato.
     * Lancia IllegalArgumentException se i valori statistici non sono validi.
     */
    private Atleta costruisciAtleta(String tipo, String nome, String sesso,
                                    int eta, int pettorale)
            throws IllegalArgumentException {
        switch (tipo) {
            case "Velocista": {
                double tempo;
                int    reazione;
                try {
                    tempo    = Double.parseDouble(TXT_Stat1.getText().trim());
                    reazione = Integer.parseInt(TXT_Stat2.getText().trim());
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException(
                            "Tempo gara (sec) e Tempo reazione (cs) devono essere numeri validi.");
                }
                Velocisti v = new Velocisti(nome, sesso, eta, pettorale);
                v.setTempoGara(tempo);
                v.setTempoReazione(reazione);
                return v;
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
                Velocisti v = new Velocisti(nome, sesso, eta, pettorale);
                v.setTempoGara(tempo);
                v.setTempoOstacolo(penalita);
                return v;
            }
            case "Fondometrista": {
                double tempo;
                try {
                    tempo = Double.parseDouble(TXT_Stat1.getText().trim());
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException(
                            "Tempo gara deve essere un numero decimale (es. 210.5).");
                }
                Velocisti v = new Velocisti(nome, sesso, eta, pettorale);
                v.setTempoGara(tempo);
                // tempoReazione e tempoOstacolo restano null → isFondometrista() == true
                return v;
            }
            case "Saltatore": {
                int distanza;
                try {
                    distanza = Integer.parseInt(TXT_Stat1.getText().trim());
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException(
                            "Distanza salto deve essere un numero intero (cm).");
                }
                Saltatori s = new Saltatori(nome, sesso, eta, pettorale);
                s.setDistanzaSalto(distanza);
                return s;
            }
            case "Pesista": {
                int distanza;
                try {
                    distanza = Integer.parseInt(TXT_Stat1.getText().trim());
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException(
                            "Distanza lancio deve essere un numero intero (cm).");
                }
                Lanciatori l = new Lanciatori(nome, sesso, eta, pettorale);
                l.setDistanzaLancio(distanza);
                return l;
            }
            default:
                throw new IllegalArgumentException("Tipo atleta non riconosciuto: " + tipo);
        }
    }

    /** Svuota tutti i campi di input dopo un inserimento riuscito. */
    private void pulisciCampi() {
        TXT_Nome.setText("");
        TXT_Eta.setText("");
        TXT_Pettorale.setText("");
        TXT_Stat1.setText("");
        TXT_Stat2.setText("");
        RBT_M.setSelected(true);
    }

    // ── Navigazione ────────────────────────────────────────────────────────

    /** ◀  Torna a FRM_Gara (Form 0). */
    private void goSinistra() {
        new FRM_Gara().setVisible(true);
        this.dispose();
    }

    /** ▶  Va a FRM_Classifica (Form 2). */
    private void goDestra() {
        new FRM_Classifica().setVisible(true);
        this.dispose();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  INIT COMPONENTS
    // ══════════════════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private void initComponents() {

        ButtonGroup grpSesso = new ButtonGroup();

        LBL_InfoGara   = new javax.swing.JLabel();
        LBL_Nome       = new javax.swing.JLabel();
        TXT_Nome       = new javax.swing.JTextField();
        LBL_Sesso      = new javax.swing.JLabel();
        RBT_M          = new javax.swing.JRadioButton();
        RBT_F          = new javax.swing.JRadioButton();
        LBL_Eta        = new javax.swing.JLabel();
        TXT_Eta        = new javax.swing.JTextField();
        LBL_Pettorale  = new javax.swing.JLabel();
        TXT_Pettorale  = new javax.swing.JTextField();
        LBL_Tipo       = new javax.swing.JLabel();
        CMB_TipoAtleta = new javax.swing.JComboBox<>();
        LBL_Stat1      = new javax.swing.JLabel();
        TXT_Stat1      = new javax.swing.JTextField();
        LBL_Stat2      = new javax.swing.JLabel();
        TXT_Stat2      = new javax.swing.JTextField();
        BTN_Aggiungi   = new javax.swing.JButton();
        jSeparator1    = new javax.swing.JSeparator();
        jScrollPane1   = new javax.swing.JScrollPane();
        LST_Atleti     = new javax.swing.JList<>();
        BTN_Sinistra   = new javax.swing.JButton();
        BTN_Destra     = new javax.swing.JButton();
        LBL_Form       = new javax.swing.JLabel();
        LBL_ListaTitolo = new javax.swing.JLabel();

        // ── finestra ──────────────────────────────────────────────────────
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Inserimento Atleti  [1 / 3]");

        // ── info gara ─────────────────────────────────────────────────────
        LBL_InfoGara.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
        LBL_InfoGara.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LBL_InfoGara.setText("...");

        // ── nome ──────────────────────────────────────────────────────────
        LBL_Nome.setText("Nome:");
        TXT_Nome.setColumns(18);

        // ── sesso ─────────────────────────────────────────────────────────
        LBL_Sesso.setText("Sesso:");
        grpSesso.add(RBT_M);
        RBT_M.setText("M");
        grpSesso.add(RBT_F);
        RBT_F.setText("F");

        // ── eta ───────────────────────────────────────────────────────────
        LBL_Eta.setText("Età:");
        TXT_Eta.setColumns(5);

        // ── pettorale ─────────────────────────────────────────────────────
        LBL_Pettorale.setText("Pettorale:");
        TXT_Pettorale.setColumns(5);

        // ── tipo atleta ───────────────────────────────────────────────────
        LBL_Tipo.setText("Tipo atleta:");

        // ── stat 1 ────────────────────────────────────────────────────────
        LBL_Stat1.setText("Statistica 1:");
        TXT_Stat1.setColumns(10);

        // ── stat 2 ────────────────────────────────────────────────────────
        LBL_Stat2.setText("Statistica 2:");
        TXT_Stat2.setColumns(10);

        // ── pulsante aggiungi ─────────────────────────────────────────────
        BTN_Aggiungi.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 13));
        BTN_Aggiungi.setText("➕  Aggiungi atleta");
        BTN_Aggiungi.setToolTipText("Iscrive l'atleta alla gara oppure lo salva come Libero");
        BTN_Aggiungi.addActionListener(e -> aggiungiAtleta());

        // ── separatore ────────────────────────────────────────────────────
        jSeparator1.setOrientation(javax.swing.SwingConstants.HORIZONTAL);

        // ── lista atleti ──────────────────────────────────────────────────
        LBL_ListaTitolo.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 11));
        LBL_ListaTitolo.setText("Atleti inseriti (gara corrente + liberi):");

        LST_Atleti.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 11));
        jScrollPane1.setViewportView(LST_Atleti);

        // ── navigazione ───────────────────────────────────────────────────
        BTN_Sinistra.setText("<");
        BTN_Sinistra.setToolTipText("Torna a Gestione Gare (Form 0)");
        BTN_Sinistra.addActionListener(e -> goSinistra());

        BTN_Destra.setText(">");
        BTN_Destra.setToolTipText("Vai a Classifica Gara (Form 2)");
        BTN_Destra.addActionListener(e -> goDestra());

        LBL_Form.setText("1 / 3");
        LBL_Form.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        // ── layout ────────────────────────────────────────────────────────
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);

        // ── horizontal ────────────────────────────────────────────────────
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)

                    // info gara (larghezza piena)
                    .addComponent(LBL_InfoGara,
                            javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)

                    // riga: Nome
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(LBL_Nome, 80, 80, 80)
                        .addGap(6)
                        .addComponent(TXT_Nome,
                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.PREFERRED_SIZE))

                    // riga: Sesso
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(LBL_Sesso, 80, 80, 80)
                        .addGap(6)
                        .addComponent(RBT_M)
                        .addGap(10)
                        .addComponent(RBT_F))

                    // riga: Età + Pettorale
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(LBL_Eta, 80, 80, 80)
                        .addGap(6)
                        .addComponent(TXT_Eta,
                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20)
                        .addComponent(LBL_Pettorale)
                        .addGap(6)
                        .addComponent(TXT_Pettorale,
                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.PREFERRED_SIZE))

                    // riga: Tipo atleta
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(LBL_Tipo, 80, 80, 80)
                        .addGap(6)
                        .addComponent(CMB_TipoAtleta,
                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.PREFERRED_SIZE))

                    // riga: Stat1
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(LBL_Stat1, 130, 130, 130)
                        .addGap(6)
                        .addComponent(TXT_Stat1,
                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.PREFERRED_SIZE))

                    // riga: Stat2 (visibile solo per Velocista e Ostacolista)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(LBL_Stat2, 130, 130, 130)
                        .addGap(6)
                        .addComponent(TXT_Stat2,
                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.PREFERRED_SIZE))

                    // pulsante aggiungi
                    .addComponent(BTN_Aggiungi)

                    // separatore
                    .addComponent(jSeparator1,
                            javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)

                    // titolo lista
                    .addComponent(LBL_ListaTitolo)

                    // lista (larghezza piena)
                    .addComponent(jScrollPane1,
                            javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)

                    // navigazione
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(BTN_Sinistra)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(LBL_Form, 40, 40, 40)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(BTN_Destra)))

                .addContainerGap(20, 20))
        );

        // ── vertical ──────────────────────────────────────────────────────
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(12, 12)
                .addComponent(LBL_InfoGara)
                .addGap(14)

                // Nome
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(LBL_Nome)
                    .addComponent(TXT_Nome, javax.swing.GroupLayout.PREFERRED_SIZE,
                            javax.swing.GroupLayout.DEFAULT_SIZE,
                            javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8)

                // Sesso
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(LBL_Sesso)
                    .addComponent(RBT_M)
                    .addComponent(RBT_F))
                .addGap(8)

                // Età + Pettorale
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(LBL_Eta)
                    .addComponent(TXT_Eta, javax.swing.GroupLayout.PREFERRED_SIZE,
                            javax.swing.GroupLayout.DEFAULT_SIZE,
                            javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(LBL_Pettorale)
                    .addComponent(TXT_Pettorale, javax.swing.GroupLayout.PREFERRED_SIZE,
                            javax.swing.GroupLayout.DEFAULT_SIZE,
                            javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8)

                // Tipo atleta
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(LBL_Tipo)
                    .addComponent(CMB_TipoAtleta, javax.swing.GroupLayout.PREFERRED_SIZE,
                            javax.swing.GroupLayout.DEFAULT_SIZE,
                            javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8)

                // Stat1
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(LBL_Stat1)
                    .addComponent(TXT_Stat1, javax.swing.GroupLayout.PREFERRED_SIZE,
                            javax.swing.GroupLayout.DEFAULT_SIZE,
                            javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6)

                // Stat2 (può essere nascosta)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(LBL_Stat2)
                    .addComponent(TXT_Stat2, javax.swing.GroupLayout.PREFERRED_SIZE,
                            javax.swing.GroupLayout.DEFAULT_SIZE,
                            javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(14)

                // pulsante
                .addComponent(BTN_Aggiungi)
                .addGap(14)

                // separatore
                .addComponent(jSeparator1,
                        javax.swing.GroupLayout.PREFERRED_SIZE, 6, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8)

                // titolo lista
                .addComponent(LBL_ListaTitolo)
                .addGap(6)

                // lista atleti
                .addComponent(jScrollPane1,
                        javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
                .addGap(10)

                // navigazione
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(BTN_Sinistra)
                    .addComponent(LBL_Form)
                    .addComponent(BTN_Destra))
                .addContainerGap(12, 12))
        );

        pack();
        setMinimumSize(new java.awt.Dimension(560, 560));
        setLocationRelativeTo(null);
    }

    // ── variabili ─────────────────────────────────────────────────────────
    private javax.swing.JButton              BTN_Aggiungi;
    private javax.swing.JButton              BTN_Sinistra;
    private javax.swing.JButton              BTN_Destra;
    private javax.swing.JComboBox<String>    CMB_TipoAtleta;
    private javax.swing.JLabel               LBL_InfoGara;
    private javax.swing.JLabel               LBL_Nome;
    private javax.swing.JLabel               LBL_Sesso;
    private javax.swing.JLabel               LBL_Eta;
    private javax.swing.JLabel               LBL_Pettorale;
    private javax.swing.JLabel               LBL_Tipo;
    private javax.swing.JLabel               LBL_Stat1;
    private javax.swing.JLabel               LBL_Stat2;
    private javax.swing.JLabel               LBL_Form;
    private javax.swing.JLabel               LBL_ListaTitolo;
    private javax.swing.JRadioButton         RBT_M;
    private javax.swing.JRadioButton         RBT_F;
    private javax.swing.JTextField           TXT_Nome;
    private javax.swing.JTextField           TXT_Eta;
    private javax.swing.JTextField           TXT_Pettorale;
    private javax.swing.JTextField           TXT_Stat1;
    private javax.swing.JTextField           TXT_Stat2;
    private javax.swing.JList<String>        LST_Atleti;
    private javax.swing.JScrollPane          jScrollPane1;
    private javax.swing.JSeparator           jSeparator1;
}
