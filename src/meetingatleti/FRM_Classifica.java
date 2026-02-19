package meetingatleti;

import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * Form 2 di 3 – Classifica della gara corrente.
 *
 * Mostra posizione (con medaglia e flag parità), pettorale, nome, sesso,
 * statistica specifica e punteggio di tutti gli atleti iscritti alla gara
 * selezionata in FRM_Gara, ordinati per punteggio decrescente.
 *
 * <p><b>v2 – integrazione GestorePunteggio:</b><br>
 * La classifica è ora prodotta da {@link GestorePunteggio#calcolaClassifica(Gara)}
 * e visualizzata come lista di {@link VocePunteggio}, con supporto a:<br>
 * – medaglie (colonna "Pos." mostra 🥇🥈🥉)<br>
 * – flag parità (posizione suffissata con "=")<br>
 * – pannello statistiche (max, min, media, pari merito)</p>
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
            LBL_Statistiche.setText(" ");
            return;
        }

        LBL_TitoloGara.setText("Classifica  –  " + gara.getNomeGara()
                + "  [" + gara.getCategoria() + "]  –  "
                + gara.getTipoDescrizione());

        if (gara.getAtleti().isEmpty()) {
            LBL_Vincitore.setText("Nessun atleta iscritto.");
            LBL_Statistiche.setText(" ");
            return;
        }

        // ── classifica arricchita via GestorePunteggio ────────────────────
        ArrayList<VocePunteggio> classifica = GestorePunteggio.calcolaClassifica(gara);

        // ── popola la tabella ─────────────────────────────────────────────
        DefaultTableModel model = (DefaultTableModel) TBL_Classifica.getModel();
        model.setRowCount(0);

        for (VocePunteggio vp : classifica) {
            Atleta a = vp.getAtleta();
            model.addRow(new Object[]{
                vp.etichettaCompleta(),        // "🥇 1" / "🥈 2=" / "4"
                a.getPettorale(),
                a.getNome(),
                a.getSesso(),
                getStatisticaLabel(a),
                vp.getPunteggio()
            });
        }

        // ── vincitore ─────────────────────────────────────────────────────
        VocePunteggio v = classifica.get(0);
        ArrayList<Atleta> pari = gara.getAtletiPariMerito();
        if (!pari.isEmpty()) {
            // pari merito al primo posto
            StringBuilder sb = new StringBuilder("🏆  Ex-aequo:  ");
            for (int i = 0; i < pari.size(); i++) {
                if (i > 0) sb.append("  –  ");
                sb.append(pari.get(i).getNome())
                  .append(" (Pett. ").append(pari.get(i).getPettorale()).append(")");
            }
            sb.append("   ").append(v.getPunteggio()).append(" pt");
            LBL_Vincitore.setText(sb.toString());
        } else {
            LBL_Vincitore.setText("🏆  Vincitore:  " + v.getAtleta().getNome()
                    + "   (Pett. " + v.getAtleta().getPettorale() + ")   –   "
                    + v.getPunteggio() + " punti");
        }

        // ── pannello statistiche ───────────────────────────────────────────
        LBL_Statistiche.setText(String.format(
            "<html><b>Max:</b> %d &nbsp;&nbsp; <b>Min:</b> %d &nbsp;&nbsp; <b>Media:</b> %.1f</html>",
            gara.getPunteggioMassimo(),
            gara.getPunteggioMinimo(),
            gara.getPunteggioMedio()
        ));
    }

    /** Restituisce la stringa della statistica specifica per tipo di atleta. */
    private String getStatisticaLabel(Atleta a) {
        if (a instanceof Velocisti) {
            Velocisti v = (Velocisti) a;
            if (v.isOstacolista())
                return v.getTempoGara() + "s  pen:" + v.getTempoOstacolo() + "cs";
            if (v.isVelocista())
                return v.getTempoGara() + "s  reaz:" + v.getTempoReazione() + "cs";
            return v.getTempoGara() + "s";
        } else if (a instanceof Saltatori) {
            return ((Saltatori) a).getDistanzaSalto() + " cm";
        } else if (a instanceof Lanciatori) {
            return ((Lanciatori) a).getDistanzaLancio() + " cm";
        }
        return "–";
    }

    // ── Navigazione ────────────────────────────────────────────────────────

    private void goSinistra() {
        new FRM_Atleti().setVisible(true);
        this.dispose();
    }

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
        LBL_Statistiche = new javax.swing.JLabel();
        BTN_Sinistra    = new javax.swing.JButton();
        BTN_Destra      = new javax.swing.JButton();
        LBL_Form        = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Classifica Gara  [2 / 3]");

        // ── titolo ────────────────────────────────────────────────────────
        LBL_TitoloGara.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14));
        LBL_TitoloGara.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LBL_TitoloGara.setText("Classifica");

        // ── tabella classifica (colonna "Pos." contiene medaglia + parità) ─
        TBL_Classifica.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"Pos.", "Pett.", "Nome", "Sesso", "Statistica", "Punteggio"}
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });
        TBL_Classifica.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TBL_Classifica.setRowHeight(22);
        TBL_Classifica.getColumnModel().getColumn(0).setPreferredWidth(60);   // Pos.
        TBL_Classifica.getColumnModel().getColumn(1).setPreferredWidth(45);   // Pett.
        TBL_Classifica.getColumnModel().getColumn(2).setPreferredWidth(150);  // Nome
        TBL_Classifica.getColumnModel().getColumn(3).setPreferredWidth(45);   // Sesso
        TBL_Classifica.getColumnModel().getColumn(4).setPreferredWidth(90);   // Statistica
        TBL_Classifica.getColumnModel().getColumn(5).setPreferredWidth(75);   // Punteggio
        jScrollPane1.setViewportView(TBL_Classifica);

        // ── vincitore ─────────────────────────────────────────────────────
        LBL_Vincitore.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
        LBL_Vincitore.setForeground(new java.awt.Color(0, 120, 0));
        LBL_Vincitore.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LBL_Vincitore.setText("🏆  Vincitore: –");

        // ── statistiche (max / min / media) ───────────────────────────────
        LBL_Statistiche.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 11));
        LBL_Statistiche.setForeground(new java.awt.Color(60, 60, 120));
        LBL_Statistiche.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LBL_Statistiche.setText(" ");

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
                    .addComponent(LBL_Statistiche,
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
                .addGap(6, 6, 6)
                .addComponent(LBL_Statistiche)
                .addGap(6, 6, 6)
                .addComponent(LBL_Vincitore)
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(BTN_Sinistra)
                    .addComponent(LBL_Form)
                    .addComponent(BTN_Destra))
                .addContainerGap(15, 15))
        );

        pack();
        setMinimumSize(new java.awt.Dimension(500, 490));
        setLocationRelativeTo(null);
    }

    // ── variabili ─────────────────────────────────────────────────────────
    private javax.swing.JButton     BTN_Destra;
    private javax.swing.JButton     BTN_Sinistra;
    private javax.swing.JLabel      LBL_Form;
    private javax.swing.JLabel      LBL_TitoloGara;
    private javax.swing.JLabel      LBL_Vincitore;
    private javax.swing.JLabel      LBL_Statistiche;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable      TBL_Classifica;
}
