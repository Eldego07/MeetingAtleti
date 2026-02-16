package meetingatleti;

import java.util.ArrayList;
import java.util.logging.Logger;
import javax.swing.*;

/**
 * Form principale per la gestione delle gare.
 *
 * ═══════════════════════════════════════════════════════════
 *  LOGICA DEL FORM:
 *
 *  LST_Gare   → lista delle gare del meeting; selezionane una
 *               per lavorarci. Tasto destro → "Nuova Gara"
 *               per crearla.
 *
 *  LST_Atleti → mostra gli atleti della gara selezionata,
 *               filtrati da CMB_TipoGara + RBT_M/F.
 *               Il filtro NON influisce sull'inserimento.
 *
 *  FILTRI (CMB_TipoGara + RBT_M/F):
 *               Agiscono SOLO su LST_Atleti (visualizzazione).
 *               Combinati: vedi solo atleti che soddisfano
 *               entrambe le condizioni (tipo E sesso).
 *
 *  BTN_Avvia  → apre FRM_Atleti per iscrivere atleti
 *               alla gara selezionata in LST_Gare.
 * ═══════════════════════════════════════════════════════════
 */
public class FRM_Gara extends javax.swing.JFrame {

    private static final Logger logger = Logger.getLogger(FRM_Gara.class.getName());

    // modelli per le due JList
    private final DefaultListModel<String> modelGare   = new DefaultListModel<>();
    private final DefaultListModel<String> modelAtleti = new DefaultListModel<>();

    // mappa indice riga → oggetto Gara reale
    private final ArrayList<Gara> indiceGare = new ArrayList<>();

    // ────────────────────────────────────────────────────────────────────────

    public FRM_Gara() {
        initComponents();

        // collega i modelli alle JList
        LST_Gare.setModel(modelGare);
        LST_Atleti.setModel(modelAtleti);
        LST_Gare.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        LST_Atleti.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // selezionare una gara in LST_Gare aggiorna LST_Atleti (con filtri)
        LST_Gare.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Gara sel = garaSelezionata();
                AppData.getInstance().setGaraCorrente(sel);
                aggiornaListaAtleti();
            }
        });

        // tasto destro su LST_Gare → menu contestuale "Nuova Gara"
        LST_Gare.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    // seleziona la riga sotto il cursore prima di mostrare il menu
                    int idx = LST_Gare.locationToIndex(e.getPoint());
                    if (idx >= 0) LST_Gare.setSelectedIndex(idx);
                    mostraMenuGara(e);
                }
            }
        });

        aggiornaListaGare();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  LOGICA APPLICATIVA
    // ══════════════════════════════════════════════════════════════════════

    // ── aggiorna liste ─────────────────────────────────────────────────────

    /** Ricarica LST_Gare dal Meeting. */
    private void aggiornaListaGare() {
        modelGare.clear();
        indiceGare.clear();
        for (Gara g : AppData.getInstance().getMeeting().getGare()) {
            modelGare.addElement(g.toString());
            indiceGare.add(g);
        }
    }

    /**
     * Ricarica LST_Atleti con gli atleti della gara selezionata,
     * applicando il filtro combinato: TIPO (CMB_TipoGara) E SESSO (RBT_M/F).
     *
     * NOTA: questo filtro riguarda SOLO la visualizzazione,
     * NON influisce su cosa viene inserito in FRM_Atleti.
     */
    private void aggiornaListaAtleti() {
        modelAtleti.clear();
        Gara sel = garaSelezionata();
        if (sel == null) return;

        // ── 1. filtro sesso ───────────────────────────────────────────────
        ArrayList<Atleta> dopoSesso;
        if (RBT_M.isSelected())      dopoSesso = sel.getAtletiM();
        else if (RBT_F.isSelected()) dopoSesso = sel.getAtletiF();
        else                         dopoSesso = sel.getAtleti(); // nessun filtro sesso

        // ── 2. filtro tipo (applicato dopo il filtro sesso) ───────────────
        String filtroTipo = (String) CMB_TipoGara.getSelectedItem();
        boolean tuttoTipo = (filtroTipo == null || filtroTipo.isBlank());

        for (Atleta a : dopoSesso) {
            if (tuttoTipo || corrispondeTipo(a, filtroTipo)) {
                modelAtleti.addElement(a.toString());
            }
        }
    }

    /**
     * Controlla se l'atleta corrisponde al tipo selezionato nel combo.
     * Usato SOLO per il filtro di visualizzazione in LST_Atleti.
     */
    private boolean corrispondeTipo(Atleta a, String tipoFiltro) {
        switch (tipoFiltro.trim()) {
            case "Velocista":
                // Velocista con velocitaCorsa > 0 (non fondometrista)
                return (a instanceof Velocisti) && ((Velocisti) a).getVelocitaCorsa() != null
                        && ((Velocisti) a).getVelocitaCorsa() > 0;
            case "Pesista":
                return a instanceof Lanciatori;
            case "Saltatore":
                return a instanceof Saltatori;
            case "Fondometrista":
                // Velocisti usati come fondometristi hanno velocitaCorsa == 0
                return (a instanceof Velocisti) && ((Velocisti) a).getVelocitaCorsa() != null
                        && ((Velocisti) a).getVelocitaCorsa() == 0;
            default:
                return true;
        }
    }

    /** Ritorna la Gara corrispondente alla riga selezionata in LST_Gare. */
    private Gara garaSelezionata() {
        int idx = LST_Gare.getSelectedIndex();
        if (idx < 0 || idx >= indiceGare.size()) return null;
        return indiceGare.get(idx);
    }

    // ── menu contestuale su LST_Gare ───────────────────────────────────────

    /** Mostra il menu contestuale (tasto destro) su LST_Gare. */
    private void mostraMenuGara(java.awt.event.MouseEvent e) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem itemNuova = new JMenuItem("➕  Nuova Gara...");
        itemNuova.addActionListener(a -> creaGaraConDialog());
        menu.add(itemNuova);

        // mostra "Elimina" solo se c'è una gara selezionata
        if (garaSelezionata() != null) {
            JMenuItem itemElimina = new JMenuItem("🗑  Elimina Gara selezionata");
            itemElimina.addActionListener(a -> eliminaGaraSelezionata());
            menu.add(itemElimina);

            JMenuItem itemClassifica = new JMenuItem("🏆  Mostra Classifica");
            itemClassifica.addActionListener(a -> mostraClassifica());
            menu.add(itemClassifica);
        }

        menu.show(LST_Gare, e.getX(), e.getY());
    }

    /** Apre un dialog per creare una nuova gara e la aggiunge al meeting. */
    private void creaGaraConDialog() {
        // nome gara
        String nomeGara = JOptionPane.showInputDialog(this,
                "Nome della gara:", "Nuova Gara", JOptionPane.PLAIN_MESSAGE);
        if (nomeGara == null || nomeGara.isBlank()) return;

        // categoria
        String[] categorie = {"M – Maschile", "F – Femminile"};
        int sceltaCat = JOptionPane.showOptionDialog(this,
                "Categoria:", "Nuova Gara",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, categorie, categorie[0]);
        if (sceltaCat < 0) return;
        String categoria = (sceltaCat == 0) ? "M" : "F";

        // tipo gara
        String[] tipi = {"Corsa", "Salto", "Lancio"};
        int sceltaTipo = JOptionPane.showOptionDialog(this,
                "Tipo di gara:", "Nuova Gara",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, tipi, tipi[0]);
        if (sceltaTipo < 0) return;

        Gara nuova = new Gara(nomeGara.trim(), categoria);
        switch (sceltaTipo) {
            case 0: // Corsa
                TipoGaraCorsa[] valoriCorsa = TipoGaraCorsa.values();
                TipoGaraCorsa sceltaCorsa = (TipoGaraCorsa) JOptionPane.showInputDialog(
                        this, "Specialità corsa:", "Nuova Gara",
                        JOptionPane.PLAIN_MESSAGE, null, valoriCorsa, valoriCorsa[0]);
                if (sceltaCorsa == null) return;
                nuova.setTipoGaraCorsa(sceltaCorsa);
                break;
            case 1: // Salto
                TipoGaraSalto[] valoriSalto = TipoGaraSalto.values();
                TipoGaraSalto sceltaSalto = (TipoGaraSalto) JOptionPane.showInputDialog(
                        this, "Specialità salto:", "Nuova Gara",
                        JOptionPane.PLAIN_MESSAGE, null, valoriSalto, valoriSalto[0]);
                if (sceltaSalto == null) return;
                nuova.setTipoGaraSalto(sceltaSalto);
                break;
            case 2: // Lancio
                TipoGaraLancio[] valoriLancio = TipoGaraLancio.values();
                TipoGaraLancio sceltaLancio = (TipoGaraLancio) JOptionPane.showInputDialog(
                        this, "Specialità lancio:", "Nuova Gara",
                        JOptionPane.PLAIN_MESSAGE, null, valoriLancio, valoriLancio[0]);
                if (sceltaLancio == null) return;
                nuova.setTipoGaraLancio(sceltaLancio);
                break;
        }

        AppData.getInstance().getMeeting().aggiungiGara(nuova);
        aggiornaListaGare();

        // seleziona automaticamente la gara appena creata
        int nuovoIdx = modelGare.size() - 1;
        LST_Gare.setSelectedIndex(nuovoIdx);
        AppData.getInstance().setGaraCorrente(nuova);
    }

    /** Elimina la gara selezionata dal meeting dopo conferma. */
    private void eliminaGaraSelezionata() {
        Gara sel = garaSelezionata();
        if (sel == null) return;
        int conf = JOptionPane.showConfirmDialog(this,
                "Eliminare la gara \"" + sel.getNomeGara() + "\"?\n"
                + "Tutti gli atleti iscritti saranno rimossi.",
                "Conferma eliminazione", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (conf == JOptionPane.YES_OPTION) {
            AppData.getInstance().getMeeting().rimuoviGara(sel);
            AppData.getInstance().setGaraCorrente(null);
            aggiornaListaGare();
            modelAtleti.clear();
        }
    }

    // ── BTN_Avvia ──────────────────────────────────────────────────────────

    /**
     * Apre FRM_Atleti per iscrivere atleti alla gara selezionata.
     * L'inserimento in FRM_Atleti è indipendente dai filtri attivi qui.
     */
    private void avviaGara() {
        Gara sel = garaSelezionata();
        if (sel == null) {
            JOptionPane.showMessageDialog(this,
                    "Seleziona prima una gara dalla lista.\n"
                    + "(Tasto destro su LST_Gare → \"Nuova Gara\" per crearne una)",
                    "Nessuna gara selezionata", JOptionPane.WARNING_MESSAGE);
            return;
        }
        AppData.getInstance().setGaraCorrente(sel);
        FRM_Atleti formAtleti = new FRM_Atleti();
        formAtleti.setVisible(true);

        // quando FRM_Atleti si chiude, aggiorna LST_Atleti con i filtri attivi
        formAtleti.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                aggiornaListaAtleti();
                aggiornaListaGare(); // aggiorna anche il conteggio atleti in LST_Gare
            }
        });
    }

    // ── classifica ─────────────────────────────────────────────────────────

    /** Mostra la classifica della gara selezionata in un popup. */
    private void mostraClassifica() {
        Gara sel = garaSelezionata();
        if (sel == null) return;
        if (sel.getAtleti().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nessun atleta iscritto a questa gara.",
                    "Classifica", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        sel.calcolaClassifica();
        ArrayList<Atleta> classifica = sel.getClassifica();

        StringBuilder sb = new StringBuilder();
        sb.append("CLASSIFICA – ").append(sel.getNomeGara())
          .append(" [").append(sel.getCategoria()).append("]\n");
        sb.append("─".repeat(55)).append("\n");
        sb.append(String.format("%-4s %-5s %-20s %-10s%n", "Pos", "Pett", "Nome", "Punteggio"));
        sb.append("─".repeat(55)).append("\n");

        for (int i = 0; i < classifica.size(); i++) {
            Atleta a = classifica.get(i);
            sb.append(String.format("%-4d %-5d %-20s %-10d%n",
                    i + 1, a.getPettorale(), a.getNome(), a.calcolaPunteggio()));
        }

        Atleta v = sel.trovaVincitore();
        sb.append("\n🏆  Vincitore: ").append(v.getNome())
          .append("  (Pett. ").append(v.getPettorale()).append(") – ")
          .append(v.calcolaPunteggio()).append(" pt");

        JTextArea area = new JTextArea(sb.toString());
        area.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
        area.setEditable(false);
        area.setRows(Math.min(classifica.size() + 6, 20));
        JOptionPane.showMessageDialog(this, new JScrollPane(area),
                "Classifica – " + sel.getNomeGara(), JOptionPane.INFORMATION_MESSAGE);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  CODICE GENERATO DA NETBEANS (initComponents)
    //  Nomi variabili identici al .form
    // ══════════════════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private void initComponents() {

        ButtonGroup buttonGroup1 = new ButtonGroup();

        CMB_TipoGara = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        LST_Atleti   = new javax.swing.JList<>();
        RBT_M        = new javax.swing.JRadioButton();
        RBT_F        = new javax.swing.JRadioButton();
        BTN_Avvia    = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        LST_Gare     = new javax.swing.JList<>();
        LBL_Fltri    = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Gestione Gare – Meeting Atletica");

        // CMB_TipoGara: filtro tipo atleta (vuoto = tutti)
        CMB_TipoGara.setModel(new javax.swing.DefaultComboBoxModel<>(
                new String[]{"Velocista", "Pesista ", "Saltatore", " "}));
        CMB_TipoGara.setSelectedIndex(3); // default = " " = tutti i tipi
        CMB_TipoGara.setToolTipText("Filtra gli atleti per tipo (solo visualizzazione)");
        CMB_TipoGara.addActionListener(evt -> CMB_TipoGaraActionPerformed(evt));

        jScrollPane1.setViewportView(LST_Atleti);

        buttonGroup1.add(RBT_M);
        RBT_M.setText("Maschio");
        RBT_M.addActionListener(evt -> RBT_MActionPerformed(evt));

        buttonGroup1.add(RBT_F);
        RBT_F.setText("Femmina");
        RBT_F.addActionListener(evt -> RBT_FActionPerformed(evt));

        BTN_Avvia.setText("Avvia");
        BTN_Avvia.setToolTipText("Apri FRM_Atleti per iscrivere atleti alla gara selezionata");
        BTN_Avvia.addActionListener(evt -> avviaGara());

        jScrollPane2.setViewportView(LST_Gare);

        LBL_Fltri.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LBL_Fltri.setText("FILTRI");
        LBL_Fltri.setToolTipText("I filtri agiscono solo sulla visualizzazione, non sull'inserimento");

        // ── layout (GroupLayout identico al .form) ────────────────────────
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);

        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(CMB_TipoGara, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(RBT_M)
                                .addGap(7, 7, 7))
                            .addComponent(RBT_F)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(71, 71, 71)
                        .addComponent(LBL_Fltri, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(48, 48, 48)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(45, 45, 45)
                .addComponent(BTN_Avvia)
                .addContainerGap(227, Short.MAX_VALUE))
        );

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addGap(11, 11, 11)
                            .addComponent(LBL_Fltri)
                            .addGap(18, 18, 18)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(CMB_TipoGara, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(RBT_M))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(RBT_F))
                        .addGroup(layout.createSequentialGroup()
                            .addGap(15, 15, 15)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 373, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(BTN_Avvia)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 373, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(26, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }

    // ── handler del .form ─────────────────────────────────────────────────

    private void CMB_TipoGaraActionPerformed(java.awt.event.ActionEvent evt) {
        // filtro cambiato → aggiorna solo la visualizzazione di LST_Atleti
        aggiornaListaAtleti();
    }

    private void RBT_MActionPerformed(java.awt.event.ActionEvent evt) {
        aggiornaListaAtleti();
    }

    private void RBT_FActionPerformed(java.awt.event.ActionEvent evt) {
        aggiornaListaAtleti();
    }

    // ── variabili (stessi nomi del .form) ─────────────────────────────────

    private javax.swing.JButton           BTN_Avvia;
    private javax.swing.JComboBox<String> CMB_TipoGara;
    private javax.swing.JLabel            LBL_Fltri;
    private javax.swing.JList<String>     LST_Atleti;
    private javax.swing.JList<String>     LST_Gare;
    private javax.swing.JRadioButton      RBT_F;
    private javax.swing.JRadioButton      RBT_M;
    private javax.swing.JScrollPane       jScrollPane1;
    private javax.swing.JScrollPane       jScrollPane2;

    // ── main per avvio standalone ─────────────────────────────────────────

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info
                    : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        java.awt.EventQueue.invokeLater(() -> new FRM_Gara().setVisible(true));
    }
}
