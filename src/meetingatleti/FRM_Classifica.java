package meetingatleti;

import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * Form 2 di 3 – Classifica della gara corrente.
 *
 * Mostra posizione, pettorale, nome, statistica e punteggio
 * di tutti gli atleti iscritti alla gara selezionata in FRM_Gara,
 * ordinati per punteggio decrescente.
 *
 * Navigazione:
 *   BTN_Sinistra  →  torna a FRM_Atleti  (Form 1)
 *   BTN_Destra    →  va a FRM_ClassificaGenerale (Form 3)
 */
public class FRM_Classifica extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(FRM_Classifica.class.getName());

    public FRM_Classifica() {
        initComponents();
        aggiornaClassifica();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  LOGICA APPLICATIVA
    // ══════════════════════════════════════════════════════════════════════

    /** Carica la classifica della gara corrente nella tabella. */
    private void aggiornaClassifica() {
        Gara gara = AppData.getInstance().getGaraCorrente();

        if (gara == null) {
            LBL_TitoloGara.setText("Nessuna gara selezionata");
            return;
        }

        LBL_TitoloGara.setText("Classifica  –  " + gara.getNomeGara()
                + "  [" + gara.getCategoria() + "]  –  "
                + gara.getTipoDescrizione());

        if (gara.getAtleti().isEmpty()) {
            LBL_Vincitore.setText("Nessun atleta iscritto.");
            return;
        }

        gara.calcolaClassifica();
        ArrayList<Atleta> classifica = gara.getClassifica();

        // popola la tabella
        DefaultTableModel model = (DefaultTableModel) TBL_Classifica.getModel();
        model.setRowCount(0); // svuota

        for (int i = 0; i < classifica.size(); i++) {
            Atleta a = classifica.get(i);
            String statLabel = getStatisticaLabel(a);
            model.addRow(new Object[]{
                i + 1,
                a.getPettorale(),
                a.getNome(),
                a.getSesso(),
                statLabel,
                a.calcolaPunteggio()
            });
        }

        // vincitore
        Atleta v = gara.trovaVincitore();
        LBL_Vincitore.setText("🏆  Vincitore:  " + v.getNome()
                + "   (Pett. " + v.getPettorale() + ")   –   "
                + v.calcolaPunteggio() + " punti");
    }

    /** Restituisce la stringa della statistica specifica per tipo di atleta. */
    private String getStatisticaLabel(Atleta a) {
        if (a instanceof Velocisti) {
            Velocisti v = (Velocisti) a;
            if (v.isOstacolista())
                return v.getTempoGara() + "s  pen:" + v.getTempoOstacolo() + "cs";
            if (v.isVelocista())
                return v.getTempoGara() + "s  reaz:" + v.getTempoReazione() + "cs";
            return v.getTempoGara() + "s";   // fondometrista
        } else if (a instanceof Saltatori) {
            return ((Saltatori) a).getDistanzaSalto() + " cm";
        } else if (a instanceof Lanciatori) {
            return ((Lanciatori) a).getDistanzaLancio() + " cm";
        }
        return "–";
    }

    // ── Navigazione ────────────────────────────────────────────────────────

    /** ◀  Torna a FRM_Atleti (Form 1). */
    private void goSinistra() {
        new FRM_Atleti().setVisible(true);
        this.dispose();
    }

    /** ▶  Va a FRM_ClassificaGenerale (Form 3). */
    private void goDestra() {
        new FRM_ClassificaGenerale().setVisible(true);
        this.dispose();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  INIT COMPONENTS
    // ══════════════════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private void initComponents() {

        LBL_TitoloGara  = new javax.swing.JLabel();
        jScrollPane1    = new javax.swing.JScrollPane();
        TBL_Classifica  = new javax.swing.JTable();
        LBL_Vincitore   = new javax.swing.JLabel();
        BTN_Sinistra    = new javax.swing.JButton();
        BTN_Destra      = new javax.swing.JButton();
        LBL_Form        = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Classifica Gara  [2 / 3]");

        // ── titolo ────────────────────────────────────────────────────────
        LBL_TitoloGara.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14));
        LBL_TitoloGara.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LBL_TitoloGara.setText("Classifica");

        // ── tabella classifica ────────────────────────────────────────────
        TBL_Classifica.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"Pos", "Pett.", "Nome", "Sesso", "Statistica", "Punteggio"}
        ) {
            // rende le celle non editabili
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });
        TBL_Classifica.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TBL_Classifica.getColumnModel().getColumn(0).setPreferredWidth(35);
        TBL_Classifica.getColumnModel().getColumn(1).setPreferredWidth(45);
        TBL_Classifica.getColumnModel().getColumn(2).setPreferredWidth(160);
        TBL_Classifica.getColumnModel().getColumn(3).setPreferredWidth(45);
        TBL_Classifica.getColumnModel().getColumn(4).setPreferredWidth(90);
        TBL_Classifica.getColumnModel().getColumn(5).setPreferredWidth(75);
        jScrollPane1.setViewportView(TBL_Classifica);

        // ── vincitore ─────────────────────────────────────────────────────
        LBL_Vincitore.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
        LBL_Vincitore.setForeground(new java.awt.Color(0, 120, 0));
        LBL_Vincitore.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LBL_Vincitore.setText("🏆  Vincitore: –");

        // ── navigazione ───────────────────────────────────────────────────
        BTN_Sinistra.setText("<");
        BTN_Sinistra.setToolTipText("Torna a Inserimento Atleti (Form 1)");
        BTN_Sinistra.addActionListener(e -> goSinistra());

        BTN_Destra.setText(">");
        BTN_Destra.setToolTipText("Vai a Classifica Generale (Form 3)");
        BTN_Destra.addActionListener(e -> goDestra());

        LBL_Form.setText("2 / 3");
        LBL_Form.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        // ── layout ────────────────────────────────────────────────────────
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);

        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(LBL_TitoloGara,
                            javax.swing.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE)
                    .addComponent(jScrollPane1,
                            javax.swing.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE)
                    .addComponent(LBL_Vincitore,
                            javax.swing.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE)
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

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(15, 15)
                .addComponent(LBL_TitoloGara)
                .addGap(10, 10, 10)
                .addComponent(jScrollPane1,
                        javax.swing.GroupLayout.PREFERRED_SIZE, 300, Short.MAX_VALUE)
                .addGap(10, 10, 10)
                .addComponent(LBL_Vincitore)
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(BTN_Sinistra)
                    .addComponent(LBL_Form)
                    .addComponent(BTN_Destra))
                .addContainerGap(15, 15))
        );

        pack();
        setMinimumSize(new java.awt.Dimension(500, 460));
        setLocationRelativeTo(null);
    }

    // ── variabili ─────────────────────────────────────────────────────────
    private javax.swing.JButton    BTN_Destra;
    private javax.swing.JButton    BTN_Sinistra;
    private javax.swing.JLabel     LBL_Form;
    private javax.swing.JLabel     LBL_TitoloGara;
    private javax.swing.JLabel     LBL_Vincitore;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable     TBL_Classifica;
}
