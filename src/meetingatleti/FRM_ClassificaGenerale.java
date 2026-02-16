package meetingatleti;

import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * Form 3 di 3 – Classifica Generale del Meeting.
 *
 * Mostra tutte le gare del meeting con il rispettivo vincitore
 * e il punteggio massimo ottenuto.
 * Nella seconda tabella, se la gara corrente è selezionata,
 * mostra anche la classifica completa di quella gara a confronto.
 *
 * Navigazione:
 *   BTN_Sinistra  →  torna a FRM_Classifica (Form 2)
 *   BTN_Destra    →  disabilitato (siamo all'ultimo form)
 */
public class FRM_ClassificaGenerale extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(FRM_ClassificaGenerale.class.getName());

    public FRM_ClassificaGenerale() {
        initComponents();
        aggiornaRiepilogo();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  LOGICA APPLICATIVA
    // ══════════════════════════════════════════════════════════════════════

    /** Popola entrambe le tabelle. */
    private void aggiornaRiepilogo() {
        Meeting meeting = AppData.getInstance().getMeeting();
        LBL_TitoloMeeting.setText("Classifica Generale  –  " + meeting.getNome()
                + "  |  " + meeting.getData() + "  |  " + meeting.getLuogo());

        populateTabellaVincitori(meeting);
        populateTabellaGara();
    }

    // ── tabella superiore: una riga per gara con il vincitore ──────────────

    private void populateTabellaVincitori(Meeting meeting) {
        DefaultTableModel model = (DefaultTableModel) TBL_Vincitori.getModel();
        model.setRowCount(0);

        for (Gara g : meeting.getGare()) {
            Atleta v = g.trovaVincitore();
            if (v != null) {
                model.addRow(new Object[]{
                    g.getNomeGara(),
                    g.getCategoria(),
                    g.getTipoDescrizione(),
                    g.getNumeroPartecipanti(),
                    v.getNome(),
                    v.getPettorale(),
                    v.calcolaPunteggio()
                });
            } else {
                model.addRow(new Object[]{
                    g.getNomeGara(),
                    g.getCategoria(),
                    g.getTipoDescrizione(),
                    0,
                    "–",
                    "–",
                    "–"
                });
            }
        }
    }

    // ── tabella inferiore: classifica completa della gara corrente ─────────

    private void populateTabellaGara() {
        DefaultTableModel model = (DefaultTableModel) TBL_GaraCorrente.getModel();
        model.setRowCount(0);

        Gara gara = AppData.getInstance().getGaraCorrente();
        if (gara == null || gara.getAtleti().isEmpty()) {
            LBL_SottoTitolo.setText("Nessuna gara selezionata");
            return;
        }

        LBL_SottoTitolo.setText("Dettaglio punteggi  –  " + gara.getNomeGara()
                + "  [" + gara.getCategoria() + "]");

        gara.calcolaClassifica();
        ArrayList<Atleta> classifica = gara.getClassifica();

        for (int i = 0; i < classifica.size(); i++) {
            Atleta a = classifica.get(i);
            model.addRow(new Object[]{
                i + 1,
                a.getPettorale(),
                a.getNome(),
                a.getSesso(),
                getStatisticaLabel(a),
                a.calcolaPunteggio()
            });
        }
    }

    private String getStatisticaLabel(Atleta a) {
        if (a instanceof Velocisti) {
            Velocisti v = (Velocisti) a;
            if (v.getVelocitaCorsa() != null && v.getVelocitaCorsa() > 0)
                return v.getVelocitaCorsa() + " km/h";
            else
                return "T.Rea: " + v.getTempoReazione() + " cs";
        } else if (a instanceof Saltatori)
            return ((Saltatori) a).getDistanzaSalto() + " cm";
        else if (a instanceof Lanciatori)
            return ((Lanciatori) a).getDistanzaLancio() + " cm";
        return "–";
    }

    // ── Navigazione ────────────────────────────────────────────────────────

    /** ◀  Torna a FRM_Classifica (Form 2). */
    private void goSinistra() {
        new FRM_Classifica().setVisible(true);
        this.dispose();
    }

    // BTN_Destra è disabilitato: siamo all'ultimo form.

    // ══════════════════════════════════════════════════════════════════════
    //  INIT COMPONENTS
    // ══════════════════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private void initComponents() {

        LBL_TitoloMeeting = new javax.swing.JLabel();
        jScrollPane1      = new javax.swing.JScrollPane();
        TBL_Vincitori     = new javax.swing.JTable();
        LBL_SottoTitolo   = new javax.swing.JLabel();
        jScrollPane2      = new javax.swing.JScrollPane();
        TBL_GaraCorrente  = new javax.swing.JTable();
        BTN_Sinistra      = new javax.swing.JButton();
        BTN_Destra        = new javax.swing.JButton();
        LBL_Form          = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Classifica Generale  [3 / 3]");

        // ── titolo meeting ────────────────────────────────────────────────
        LBL_TitoloMeeting.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14));
        LBL_TitoloMeeting.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LBL_TitoloMeeting.setText("Classifica Generale");

        // ── tabella vincitori ─────────────────────────────────────────────
        TBL_Vincitori.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"Gara", "Cat.", "Specialità", "Part.", "Vincitore", "Pett.", "Punteggio"}
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });
        TBL_Vincitori.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TBL_Vincitori.getColumnModel().getColumn(0).setPreferredWidth(130);
        TBL_Vincitori.getColumnModel().getColumn(1).setPreferredWidth(35);
        TBL_Vincitori.getColumnModel().getColumn(2).setPreferredWidth(100);
        TBL_Vincitori.getColumnModel().getColumn(3).setPreferredWidth(40);
        TBL_Vincitori.getColumnModel().getColumn(4).setPreferredWidth(140);
        TBL_Vincitori.getColumnModel().getColumn(5).setPreferredWidth(40);
        TBL_Vincitori.getColumnModel().getColumn(6).setPreferredWidth(70);
        jScrollPane1.setViewportView(TBL_Vincitori);

        // ── sottotitolo tabella dettaglio ─────────────────────────────────
        LBL_SottoTitolo.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
        LBL_SottoTitolo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LBL_SottoTitolo.setText("Dettaglio punteggi – gara corrente");

        // ── tabella classifica della gara corrente ────────────────────────
        TBL_GaraCorrente.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"Pos", "Pett.", "Nome", "Sesso", "Statistica", "Punteggio"}
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });
        TBL_GaraCorrente.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TBL_GaraCorrente.getColumnModel().getColumn(0).setPreferredWidth(35);
        TBL_GaraCorrente.getColumnModel().getColumn(1).setPreferredWidth(45);
        TBL_GaraCorrente.getColumnModel().getColumn(2).setPreferredWidth(160);
        TBL_GaraCorrente.getColumnModel().getColumn(3).setPreferredWidth(45);
        TBL_GaraCorrente.getColumnModel().getColumn(4).setPreferredWidth(90);
        TBL_GaraCorrente.getColumnModel().getColumn(5).setPreferredWidth(75);
        jScrollPane2.setViewportView(TBL_GaraCorrente);

        // ── navigazione ───────────────────────────────────────────────────
        BTN_Sinistra.setText("<");
        BTN_Sinistra.setToolTipText("Torna a Classifica Gara (Form 2)");
        BTN_Sinistra.addActionListener(e -> goSinistra());

        BTN_Destra.setText(">");
        BTN_Destra.setEnabled(false); // ultimo form
        BTN_Destra.setToolTipText("Sei già all'ultimo form");

        LBL_Form.setText("3 / 3");
        LBL_Form.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        // ── layout ────────────────────────────────────────────────────────
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);

        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(LBL_TitoloMeeting,
                            javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
                    .addComponent(jScrollPane1,
                            javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
                    .addComponent(LBL_SottoTitolo,
                            javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
                    .addComponent(jScrollPane2,
                            javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
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
                .addComponent(LBL_TitoloMeeting)
                .addGap(10, 10, 10)
                .addComponent(jScrollPane1,
                        javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(LBL_SottoTitolo)
                .addGap(6, 6, 6)
                .addComponent(jScrollPane2,
                        javax.swing.GroupLayout.PREFERRED_SIZE, 200, Short.MAX_VALUE)
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(BTN_Sinistra)
                    .addComponent(LBL_Form)
                    .addComponent(BTN_Destra))
                .addContainerGap(15, 15))
        );

        pack();
        setMinimumSize(new java.awt.Dimension(600, 560));
        setLocationRelativeTo(null);
    }

    // ── variabili ─────────────────────────────────────────────────────────
    private javax.swing.JButton    BTN_Destra;
    private javax.swing.JButton    BTN_Sinistra;
    private javax.swing.JLabel     LBL_Form;
    private javax.swing.JLabel     LBL_SottoTitolo;
    private javax.swing.JLabel     LBL_TitoloMeeting;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable     TBL_GaraCorrente;
    private javax.swing.JTable     TBL_Vincitori;
}
