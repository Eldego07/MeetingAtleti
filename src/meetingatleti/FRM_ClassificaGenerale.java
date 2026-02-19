package meetingatleti;

import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * Form 3 di 3 – Classifica Generale del Meeting.
 *
 * Mostra:
 *  – Tabella superiore: tutte le gare del meeting con vincitore e punteggio massimo.
 *    Cliccando su una riga si aggiorna la tabella inferiore con la classifica
 *    completa di quella gara.
 *  – Tabella inferiore: classifica completa della gara selezionata con medaglie
 *    e flag di parità, prodotta da {@link GestorePunteggio}.
 *  – Tabella generale: classifica aggregata del meeting (totale punti per atleta
 *    su tutte le gare), con medaglie e flag di parità.
 *
 * <p><b>v2 – integrazione GestorePunteggio:</b><br>
 * La classifica generale è prodotta da
 * {@link GestorePunteggio#calcolaClassificaMeeting(Meeting)} e visualizzata
 * come lista di {@link VocePunteggioMeeting} nella terza tabella.</p>
 *
 * Navigazione:
 *   BTN_Sinistra  →  torna a FRM_Classifica (Form 2)
 *   BTN_Destra    →  disabilitato (siamo all'ultimo form)
 */
public class FRM_ClassificaGenerale extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(FRM_ClassificaGenerale.class.getName());

    /** Mappa riga di TBL_Vincitori → oggetto Gara reale. */
    private final java.util.List<Gara> indiceGare = new ArrayList<>();

    public FRM_ClassificaGenerale() {
        initComponents();
        aggiornaRiepilogo();
        // selezionare una riga in TBL_Vincitori aggiorna TBL_GaraCorrente
        TBL_Vincitori.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = TBL_Vincitori.getSelectedRow();
                if (row >= 0 && row < indiceGare.size()) {
                    Gara sel = indiceGare.get(row);
                    AppData.getInstance().setGaraCorrente(sel);
                    LBL_SottoTitolo.setText("Dettaglio punteggi  –  "
                            + sel.getNomeGara() + "  [" + sel.getCategoria() + "]");
                    populateTabellaGara(sel);
                }
            }
        });
    }

    // ══════════════════════════════════════════════════════════════════════
    //  LOGICA APPLICATIVA
    // ══════════════════════════════════════════════════════════════════════

    /** Popola tutte le tabelle. */
    private void aggiornaRiepilogo() {
        Meeting meeting = AppData.getInstance().getMeeting();
        LBL_TitoloMeeting.setText("Classifica Generale  –  " + meeting.getNome()
                + "  |  " + meeting.getData() + "  |  " + meeting.getLuogo());

        populateTabellaVincitori(meeting);
        populateTabellaGenerale(meeting);

        // pre-seleziona la gara corrente (se presente)
        Gara garaCorrente = AppData.getInstance().getGaraCorrente();
        if (garaCorrente != null) {
            populateTabellaGara(garaCorrente);
            LBL_SottoTitolo.setText("Dettaglio punteggi  –  "
                    + garaCorrente.getNomeGara() + "  [" + garaCorrente.getCategoria() + "]");
        } else {
            populateTabellaGara(null);
        }
    }

    // ── tabella superiore: una riga per gara con vincitore ─────────────────

    private void populateTabellaVincitori(Meeting meeting) {
        DefaultTableModel model = (DefaultTableModel) TBL_Vincitori.getModel();
        model.setRowCount(0);
        indiceGare.clear();

        for (Gara g : meeting.getGare()) {
            indiceGare.add(g);
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
                    0, "–", "–", "–"
                });
            }
        }
    }

    // ── tabella centrale: classifica completa gara selezionata ─────────────

    private void populateTabellaGara(Gara gara) {
        DefaultTableModel model = (DefaultTableModel) TBL_GaraCorrente.getModel();
        model.setRowCount(0);

        if (gara == null || gara.getAtleti().isEmpty()) {
            LBL_SottoTitolo.setText("Seleziona una gara dalla tabella superiore");
            return;
        }

        // classifica arricchita con medaglie via GestorePunteggio
        ArrayList<VocePunteggio> classifica = GestorePunteggio.calcolaClassifica(gara);

        for (VocePunteggio vp : classifica) {
            Atleta a = vp.getAtleta();
            model.addRow(new Object[]{
                vp.etichettaCompleta(),
                a.getPettorale(),
                a.getNome(),
                a.getSesso(),
                getStatisticaLabel(a),
                vp.getPunteggio()
            });
        }
    }

    // ── tabella inferiore: classifica generale del meeting ─────────────────

    /**
     * Popola TBL_ClassificaGenerale con i dati prodotti da
     * {@link GestorePunteggio#calcolaClassificaMeeting(Meeting)}.
     */
    private void populateTabellaGenerale(Meeting meeting) {
        DefaultTableModel model = (DefaultTableModel) TBL_ClassificaGenerale.getModel();
        model.setRowCount(0);

        ArrayList<VocePunteggioMeeting> classifica =
                GestorePunteggio.calcolaClassificaMeeting(meeting);

        if (classifica.isEmpty()) {
            LBL_TitoloGenerale.setText("Classifica Generale – nessun atleta registrato");
            return;
        }

        LBL_TitoloGenerale.setText("Classifica Generale – totale punti per atleta");

        for (VocePunteggioMeeting vp : classifica) {
            model.addRow(new Object[]{
                vp.getMedaglia().getSimbolo() + " " + vp.posizioneLabel(),
                vp.getPettorale(),
                vp.getNomeAtleta(),
                vp.getSesso(),
                vp.getNumeroGare(),
                String.format("%.1f", vp.punteggioMedio()),
                vp.getTotalePunti()
            });
        }

        // mostra miglior atleta del meeting
        VocePunteggioMeeting migliore = classifica.get(0);
        LBL_MiglioreAtleta.setText("🏆  Atleta del Meeting:  "
                + migliore.getNomeAtleta()
                + "  (Pett. " + migliore.getPettorale() + ")"
                + "  –  " + migliore.getTotalePunti() + " pt totali"
                + "  su " + migliore.getNumeroGare() + " gare");
    }

    private String getStatisticaLabel(Atleta a) {
        if (a instanceof Velocisti) {
            Velocisti v = (Velocisti) a;
            if (v.isOstacolista())
                return v.getTempoGara() + "s  pen:" + v.getTempoOstacolo() + "cs";
            if (v.isVelocista())
                return v.getTempoGara() + "s  reaz:" + v.getTempoReazione() + "cs";
            return v.getTempoGara() + "s";
        } else if (a instanceof Saltatori)
            return ((Saltatori) a).getDistanzaSalto() + " cm";
        else if (a instanceof Lanciatori)
            return ((Lanciatori) a).getDistanzaLancio() + " cm";
        return "–";
    }

    // ── Navigazione ────────────────────────────────────────────────────────

    private void goSinistra() {
        new FRM_Classifica().setVisible(true);
        this.dispose();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  INIT COMPONENTS
    // ══════════════════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private void initComponents() {

        LBL_TitoloMeeting       = new javax.swing.JLabel();
        jScrollPane1            = new javax.swing.JScrollPane();
        TBL_Vincitori           = new javax.swing.JTable();
        LBL_SottoTitolo         = new javax.swing.JLabel();
        jScrollPane2            = new javax.swing.JScrollPane();
        TBL_GaraCorrente        = new javax.swing.JTable();
        LBL_TitoloGenerale      = new javax.swing.JLabel();
        jScrollPane3            = new javax.swing.JScrollPane();
        TBL_ClassificaGenerale  = new javax.swing.JTable();
        LBL_MiglioreAtleta      = new javax.swing.JLabel();
        BTN_Sinistra            = new javax.swing.JButton();
        BTN_Destra              = new javax.swing.JButton();
        LBL_Form                = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Classifica Generale  [3 / 3]");

        // ── titolo meeting ─────────────────────────────────────────────────
        LBL_TitoloMeeting.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14));
        LBL_TitoloMeeting.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LBL_TitoloMeeting.setText("Classifica Generale");

        // ── tabella vincitori ──────────────────────────────────────────────
        TBL_Vincitori.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"Gara", "Cat.", "Specialità", "Part.", "Vincitore", "Pett.", "Punteggio"}
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });
        TBL_Vincitori.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TBL_Vincitori.setRowHeight(20);
        TBL_Vincitori.getColumnModel().getColumn(0).setPreferredWidth(130);
        TBL_Vincitori.getColumnModel().getColumn(1).setPreferredWidth(35);
        TBL_Vincitori.getColumnModel().getColumn(2).setPreferredWidth(100);
        TBL_Vincitori.getColumnModel().getColumn(3).setPreferredWidth(40);
        TBL_Vincitori.getColumnModel().getColumn(4).setPreferredWidth(140);
        TBL_Vincitori.getColumnModel().getColumn(5).setPreferredWidth(40);
        TBL_Vincitori.getColumnModel().getColumn(6).setPreferredWidth(70);
        jScrollPane1.setViewportView(TBL_Vincitori);

        // ── sottotitolo ────────────────────────────────────────────────────
        LBL_SottoTitolo.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
        LBL_SottoTitolo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LBL_SottoTitolo.setText("Seleziona una gara dalla tabella superiore");

        // ── tabella classifica gara corrente ───────────────────────────────
        TBL_GaraCorrente.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"Pos.", "Pett.", "Nome", "Sesso", "Statistica", "Punteggio"}
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });
        TBL_GaraCorrente.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TBL_GaraCorrente.setRowHeight(22);
        TBL_GaraCorrente.getColumnModel().getColumn(0).setPreferredWidth(60);
        TBL_GaraCorrente.getColumnModel().getColumn(1).setPreferredWidth(45);
        TBL_GaraCorrente.getColumnModel().getColumn(2).setPreferredWidth(150);
        TBL_GaraCorrente.getColumnModel().getColumn(3).setPreferredWidth(45);
        TBL_GaraCorrente.getColumnModel().getColumn(4).setPreferredWidth(90);
        TBL_GaraCorrente.getColumnModel().getColumn(5).setPreferredWidth(75);
        jScrollPane2.setViewportView(TBL_GaraCorrente);

        // ── titolo classifica generale ─────────────────────────────────────
        LBL_TitoloGenerale.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
        LBL_TitoloGenerale.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LBL_TitoloGenerale.setText("Classifica Generale – totale punti per atleta");

        // ── tabella classifica generale del meeting ────────────────────────
        TBL_ClassificaGenerale.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"Pos.", "Pett.", "Nome", "Sesso", "Gare", "Media", "Totale Punti"}
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });
        TBL_ClassificaGenerale.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TBL_ClassificaGenerale.setRowHeight(22);
        TBL_ClassificaGenerale.getColumnModel().getColumn(0).setPreferredWidth(60);
        TBL_ClassificaGenerale.getColumnModel().getColumn(1).setPreferredWidth(45);
        TBL_ClassificaGenerale.getColumnModel().getColumn(2).setPreferredWidth(150);
        TBL_ClassificaGenerale.getColumnModel().getColumn(3).setPreferredWidth(45);
        TBL_ClassificaGenerale.getColumnModel().getColumn(4).setPreferredWidth(40);
        TBL_ClassificaGenerale.getColumnModel().getColumn(5).setPreferredWidth(60);
        TBL_ClassificaGenerale.getColumnModel().getColumn(6).setPreferredWidth(80);
        jScrollPane3.setViewportView(TBL_ClassificaGenerale);

        // ── miglior atleta del meeting ─────────────────────────────────────
        LBL_MiglioreAtleta.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
        LBL_MiglioreAtleta.setForeground(new java.awt.Color(0, 100, 0));
        LBL_MiglioreAtleta.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LBL_MiglioreAtleta.setText("🏆  Atleta del Meeting: –");

        // ── navigazione ───────────────────────────────────────────────────
        BTN_Sinistra.setText("<");
        BTN_Sinistra.setToolTipText("Torna a Classifica Gara (Form 2)");
        BTN_Sinistra.addActionListener(e -> goSinistra());

        BTN_Destra.setText(">");
        BTN_Destra.setEnabled(false);
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
                    .addComponent(LBL_TitoloMeeting,       javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
                    .addComponent(jScrollPane1,             javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
                    .addComponent(LBL_SottoTitolo,         javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
                    .addComponent(jScrollPane2,             javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
                    .addComponent(LBL_TitoloGenerale,      javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
                    .addComponent(jScrollPane3,             javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
                    .addComponent(LBL_MiglioreAtleta,      javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
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
                .addGap(8, 8, 8)
                .addComponent(jScrollPane1,  javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(LBL_SottoTitolo)
                .addGap(4, 4, 4)
                .addComponent(jScrollPane2,  javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(LBL_TitoloGenerale)
                .addGap(4, 4, 4)
                .addComponent(jScrollPane3,  javax.swing.GroupLayout.PREFERRED_SIZE, 150, Short.MAX_VALUE)
                .addGap(6, 6, 6)
                .addComponent(LBL_MiglioreAtleta)
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(BTN_Sinistra)
                    .addComponent(LBL_Form)
                    .addComponent(BTN_Destra))
                .addContainerGap(15, 15))
        );

        pack();
        setMinimumSize(new java.awt.Dimension(600, 720));
        setLocationRelativeTo(null);
    }

    // ── variabili ─────────────────────────────────────────────────────────
    private javax.swing.JButton     BTN_Destra;
    private javax.swing.JButton     BTN_Sinistra;
    private javax.swing.JLabel      LBL_Form;
    private javax.swing.JLabel      LBL_SottoTitolo;
    private javax.swing.JLabel      LBL_TitoloMeeting;
    private javax.swing.JLabel      LBL_TitoloGenerale;
    private javax.swing.JLabel      LBL_MiglioreAtleta;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable      TBL_Vincitori;
    private javax.swing.JTable      TBL_GaraCorrente;
    private javax.swing.JTable      TBL_ClassificaGenerale;
}
