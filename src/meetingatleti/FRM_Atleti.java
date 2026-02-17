package meetingatleti;

import java.util.logging.Logger;
import javax.swing.*;

/**
 * Form 1 di 3 – Inserimento atleti.
 *
 * Componenti del .form originali (nomi invariati):
 *   LBL_Nome / TXT_Nome
 *   LBL_Sesso / TXT_Sesso
 *   LBL_Eta / TXT_Eta
 *   LBL_NMaglia / TXT_NMaglia          ← pettorale
 *   LBL_Tipo / CMB_Tipo
 *   LBL_StatisticaUnica / TXT_StatisticaUnica  ← tempo gara / distanza
 *   BTN_Inserisci, BTN_Sinistra, BTN_Destra
 *
 * Componente AGGIUNTO (non nel .form ma necessario per le interfacce UML):
 *   LBL_Statistica2 / TXT_Statistica2
 *     → visibile solo per Velocista  (tempoReazione  – interfaccia Fondometrista)
 *     → visibile solo per Ostacolista (tempoOstacolo – interfaccia Ostacolista)
 *     → nascosto per Fondometrista, Pesista, Saltatore
 */
public class FRM_Atleti extends javax.swing.JFrame {

    private static final Logger logger = Logger.getLogger(FRM_Atleti.class.getName());

    public FRM_Atleti() {
        initComponents();
        Gara g = AppData.getInstance().getGaraCorrente();
        if (g != null)
            setTitle("Inserimento Atleti  [1/3]  →  " + g.getNomeGara() + "  [" + g.getCategoria() + "]");
        aggiornaCMBTipo();
        aggiornaCampiTipo();   // imposta label + visibilità al primo avvio
    }

    // ══════════════════════════════════════════════════════════════════════
    //  POPOLA CMB_Tipo in base al tipo di gara corrente
    // ══════════════════════════════════════════════════════════════════════

    private void aggiornaCMBTipo() {
        Gara g = AppData.getInstance().getGaraCorrente();
        CMB_Tipo.removeAllItems();
        if (g == null) {
            CMB_Tipo.addItem("Velocista");
            CMB_Tipo.addItem("Fondometrista");
            CMB_Tipo.addItem("Ostacolista");
            CMB_Tipo.addItem("Pesista");
            CMB_Tipo.addItem("Saltatore");
        } else if (g.getTipoGaraCorsa() != null) {
            CMB_Tipo.addItem("Velocista");
            CMB_Tipo.addItem("Fondometrista");
            CMB_Tipo.addItem("Ostacolista");
        } else if (g.getTipoGaraSalto() != null) {
            CMB_Tipo.addItem("Saltatore");
        } else if (g.getTipoGaraLancio() != null) {
            CMB_Tipo.addItem("Pesista");
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  AGGIORNA LABEL E VISIBILITÀ DEI CAMPI IN BASE AL TIPO
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Chiamato ogni volta che CMB_Tipo cambia.
     *
     *  Velocista     → TXT_StatisticaUnica = "Tempo gara (s)"
     *                  TXT_Statistica2     = "T.Reazione (cs)"  VISIBILE
     *                    (interfaccia Fondometrista – tempoReazione allo sparo)
     *
     *  Ostacolista   → TXT_StatisticaUnica = "Tempo gara (s)"
     *                  TXT_Statistica2     = "T.Ostacolo (cs)"  VISIBILE
     *                    (interfaccia Ostacolista – penalità per ostacolo abbattuto)
     *
     *  Fondometrista → TXT_StatisticaUnica = "Tempo gara (s)"
     *                  TXT_Statistica2     = NASCOSTO
     *
     *  Pesista       → TXT_StatisticaUnica = "Lancio (cm)"
     *                  TXT_Statistica2     = NASCOSTO
     *
     *  Saltatore     → TXT_StatisticaUnica = "Salto (cm)"
     *                  TXT_Statistica2     = NASCOSTO
     */
    private void aggiornaCampiTipo() {
        String tipo = (String) CMB_Tipo.getSelectedItem();
        if (tipo == null) return;

        switch (tipo) {
            case "Velocista":
                LBL_StatisticaUnica.setText("Tempo gara (s):");
                LBL_Statistica2.setText("T.Reazione (cs):");
                LBL_Statistica2.setVisible(true);
                TXT_Statistica2.setVisible(true);
                TXT_Statistica2.setText("15");   // default: 15cs = 0.15s (reazione tipica)
                break;
            case "Ostacolista":
                LBL_StatisticaUnica.setText("Tempo gara (s):");
                LBL_Statistica2.setText("T.Ostacolo (cs):");
                LBL_Statistica2.setVisible(true);
                TXT_Statistica2.setVisible(true);
                TXT_Statistica2.setText("0");    // default: 0 ostacoli abbattuti
                break;
            case "Fondometrista":
                LBL_StatisticaUnica.setText("Tempo gara (s):");
                LBL_Statistica2.setVisible(false);
                TXT_Statistica2.setVisible(false);
                TXT_Statistica2.setText("0");
                break;
            case "Pesista":
                LBL_StatisticaUnica.setText("Lancio (cm):");
                LBL_Statistica2.setVisible(false);
                TXT_Statistica2.setVisible(false);
                TXT_Statistica2.setText("0");
                break;
            case "Saltatore":
                LBL_StatisticaUnica.setText("Salto (cm):");
                LBL_Statistica2.setVisible(false);
                TXT_Statistica2.setVisible(false);
                TXT_Statistica2.setText("0");
                break;
        }
        pack();  // ridimensiona la finestra quando i campi appaiono/scompaiono
    }

    // ══════════════════════════════════════════════════════════════════════
    //  INSERIMENTO ATLETA
    // ══════════════════════════════════════════════════════════════════════

    private void inserisciAtleta() {
        Gara gara = AppData.getInstance().getGaraCorrente();
        if (gara == null) { errore("Nessuna gara selezionata!\nTorna su FRM_Gara e seleziona una gara."); return; }

        String nome  = TXT_Nome.getText().trim();
        String sesso = TXT_Sesso.getText().trim().toUpperCase();
        String tipo  = (String) CMB_Tipo.getSelectedItem();

        if (nome.isEmpty()) { errore("Inserisci il nome."); return; }
        if (!sesso.equals("M") && !sesso.equals("F")) { errore("Sesso: M oppure F."); return; }

        int eta, nMaglia;
        try { eta = Integer.parseInt(TXT_Eta.getText().trim()); }
        catch (NumberFormatException e) { errore("Età non valida."); return; }

        try { nMaglia = Integer.parseInt(TXT_NMaglia.getText().trim()); }
        catch (NumberFormatException e) { errore("Numero maglia non valido."); return; }

        // ── statistica principale (tempo gara o distanza) ─────────────────
        int statistica;
        try {
            statistica = Integer.parseInt(TXT_StatisticaUnica.getText().trim());
            if (statistica <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            errore(LBL_StatisticaUnica.getText() + " non valida (numero intero > 0)."); return;
        }

        // ── statistica secondaria (reazione / penalità ostacolo) ──────────
        int statistica2 = 0;
        if (TXT_Statistica2.isVisible()) {
            try {
                statistica2 = Integer.parseInt(TXT_Statistica2.getText().trim());
                if (statistica2 < 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                errore(LBL_Statistica2.getText() + " non valida (numero intero ≥ 0)."); return;
            }
        }

        // ── crea l'atleta del tipo corretto ───────────────────────────────
        Atleta atleta;
        switch (tipo) {
            case "Velocista": {
                // usa interfaccia Fondometrista → setTempoReazione()
                Velocisti v = new Velocisti(nome, sesso, eta, nMaglia);
                v.setTempoGara((double) statistica);
                v.setTempoReazione(statistica2);   // ← da TXT_Statistica2
                v.setTempoOstacolo(0);
                atleta = v;
                break;
            }
            case "Ostacolista": {
                // usa interfaccia Ostacolista → setTempoOstacolo()
                Velocisti o = new Velocisti(nome, sesso, eta, nMaglia);
                o.setTempoGara((double) statistica);
                o.setTempoReazione(0);
                o.setTempoOstacolo(statistica2);   // ← da TXT_Statistica2
                atleta = o;
                break;
            }
            case "Fondometrista": {
                Velocisti f = new Velocisti(nome, sesso, eta, nMaglia);
                f.setTempoGara((double) statistica);
                f.setTempoReazione(0);
                f.setTempoOstacolo(0);
                atleta = f;
                break;
            }
            case "Pesista": {
                // usa interfaccia ILanciatore → setDistanzaLancio()
                Lanciatori l = new Lanciatori(nome, sesso, eta, nMaglia);
                l.setDistanzaLancio(statistica);
                atleta = l;
                break;
            }
            case "Saltatore": {
                // usa interfaccia ISaltatore → setDistanzaSalto()
                Saltatori s = new Saltatori(nome, sesso, eta, nMaglia);
                s.setDistanzaSalto(statistica);
                atleta = s;
                break;
            }
            default:
                errore("Tipo atleta non riconosciuto."); return;
        }

        // ── iscrizione con tutti i controlli (sesso, tipo, pettorale) ─────
        boolean ok = gara.iscrizione(atleta);
        if (ok) {
            JOptionPane.showMessageDialog(this,
                    "✔  Iscritto con successo!\n" + atleta,
                    "OK", JOptionPane.INFORMATION_MESSAGE);
            pulisciCampi();
        } else {
            String motivoTipo = "";
            if (gara.getTipoGaraCorsa()  != null && !(atleta instanceof Velocisti))
                motivoTipo = "• Gara di CORSA: accetta solo Velocista / Fondometrista / Ostacolista\n";
            else if (gara.getTipoGaraSalto()  != null && !(atleta instanceof Saltatori))
                motivoTipo = "• Gara di SALTO: accetta solo Saltatore\n";
            else if (gara.getTipoGaraLancio() != null && !(atleta instanceof Lanciatori))
                motivoTipo = "• Gara di LANCIO: accetta solo Pesista\n";
            errore("Iscrizione fallita.\n"
                    + motivoTipo
                    + "• Numero maglia " + nMaglia + " già usato, oppure\n"
                    + "• Sesso '" + sesso + "' ≠ categoria '" + gara.getCategoria() + "'");
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  NAVIGAZIONE FORM
    // ══════════════════════════════════════════════════════════════════════

    private void goDestra() {
        new FRM_Classifica().setVisible(true);
        this.dispose();
    }

    private void pulisciCampi() {
        TXT_Nome.setText("");
        TXT_Sesso.setText("");
        TXT_Eta.setText("");
        TXT_NMaglia.setText("");
        TXT_StatisticaUnica.setText("");
        TXT_Statistica2.setText("");
    }

    private void errore(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Errore", JOptionPane.ERROR_MESSAGE);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  initComponents  –  layout con GroupLayout
    // ══════════════════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private void initComponents() {

        // componenti del .form originale
        LBL_Nome            = new JLabel("Nome");
        TXT_Nome            = new JTextField();
        LBL_Sesso           = new JLabel("Sesso");
        TXT_Sesso           = new JTextField();
        LBL_Eta             = new JLabel("Età");
        TXT_Eta             = new JTextField();
        LBL_NMaglia         = new JLabel("nMaglia");
        TXT_NMaglia         = new JTextField();
        LBL_Tipo            = new JLabel("Tipo");
        CMB_Tipo            = new JComboBox<>();
        LBL_StatisticaUnica = new JLabel("Statistica Unica");
        TXT_StatisticaUnica = new JTextField();
        BTN_Inserisci       = new JButton("Inserisci");
        BTN_Sinistra        = new JButton("<");
        BTN_Destra          = new JButton(">");

        // componente aggiunto: seconda statistica (reazione / ostacolo)
        LBL_Statistica2     = new JLabel("Statistica 2");
        TXT_Statistica2     = new JTextField();
        LBL_Statistica2.setVisible(false);
        TXT_Statistica2.setVisible(false);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Iscrizione Atleti");

        CMB_Tipo.setModel(new javax.swing.DefaultComboBoxModel<>(
                new String[]{"Velocista", "Pesista", "Saltatore", "Fondometrista"}));
        CMB_Tipo.addActionListener(e -> aggiornaCampiTipo());

        BTN_Inserisci.addActionListener(e -> inserisciAtleta());
        BTN_Destra.setToolTipText("Vai a Classifica Gara (Form 2)");
        BTN_Destra.addActionListener(e -> goDestra());
        BTN_Sinistra.setEnabled(false);
        BTN_Sinistra.setToolTipText("Sei già al primo form");

        TXT_NMaglia.addActionListener(e -> {});

        // ── GroupLayout ───────────────────────────────────────────────────
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);

        int LW = 120;  // larghezza colonna label
        int FW = 120;  // larghezza colonna field

        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            // ── riga pulsanti navigazione ────────────────────────────────
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                layout.createSequentialGroup()
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(BTN_Sinistra)
                    .addGap(40)
                    .addComponent(BTN_Destra)
                    .addContainerGap())
            // ── colonne label + field ────────────────────────────────────
            .addGroup(layout.createSequentialGroup()
                .addGap(20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(LBL_Nome,            0, LW, Short.MAX_VALUE)
                    .addComponent(LBL_Sesso,           0, LW, Short.MAX_VALUE)
                    .addComponent(LBL_Eta,             0, LW, Short.MAX_VALUE)
                    .addComponent(LBL_NMaglia,         0, LW, Short.MAX_VALUE)
                    .addComponent(LBL_Tipo,            0, LW, Short.MAX_VALUE)
                    .addComponent(LBL_StatisticaUnica, 0, LW, Short.MAX_VALUE)
                    .addComponent(LBL_Statistica2,     0, LW, Short.MAX_VALUE))
                .addGap(12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(TXT_Nome,            0, FW, Short.MAX_VALUE)
                    .addComponent(TXT_Sesso,           0, FW, Short.MAX_VALUE)
                    .addComponent(TXT_Eta,             0, FW, Short.MAX_VALUE)
                    .addComponent(TXT_NMaglia,         0, FW, Short.MAX_VALUE)
                    .addComponent(CMB_Tipo,            0, FW, Short.MAX_VALUE)
                    .addComponent(TXT_StatisticaUnica, 0, FW, Short.MAX_VALUE)
                    .addComponent(TXT_Statistica2,     0, FW, Short.MAX_VALUE)
                    .addComponent(BTN_Inserisci,       0, FW, Short.MAX_VALUE))
                .addGap(20))
        );

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(18)
                .addGroup(pair(layout, LBL_Nome,            TXT_Nome))
                .addGap(8)
                .addGroup(pair(layout, LBL_Sesso,           TXT_Sesso))
                .addGap(8)
                .addGroup(pair(layout, LBL_Eta,             TXT_Eta))
                .addGap(8)
                .addGroup(pair(layout, LBL_NMaglia,         TXT_NMaglia))
                .addGap(8)
                .addGroup(pair(layout, LBL_Tipo,            CMB_Tipo))
                .addGap(8)
                .addGroup(pair(layout, LBL_StatisticaUnica, TXT_StatisticaUnica))
                .addGap(8)
                .addGroup(pair(layout, LBL_Statistica2,     TXT_Statistica2))
                .addGap(12)
                .addComponent(BTN_Inserisci)
                .addGap(18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(BTN_Sinistra)
                    .addComponent(BTN_Destra))
                .addGap(12))
        );

        pack();
        setLocationRelativeTo(null);
    }

    /** Helper: crea un gruppo orizzontale baseline per una coppia label+field. */
    private javax.swing.GroupLayout.Group pair(javax.swing.GroupLayout l,
                                               JComponent label, JComponent field) {
        return l.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(label)
                .addComponent(field, javax.swing.GroupLayout.PREFERRED_SIZE,
                        javax.swing.GroupLayout.DEFAULT_SIZE,
                        javax.swing.GroupLayout.PREFERRED_SIZE);
    }

    // ── variabili (nomi del .form invariati + TXT/LBL_Statistica2 aggiunti) ──

    private JButton               BTN_Destra;
    private JButton               BTN_Inserisci;
    private JButton               BTN_Sinistra;
    private JComboBox<String>     CMB_Tipo;
    private JLabel                LBL_Eta;
    private JLabel                LBL_NMaglia;
    private JLabel                LBL_Nome;
    private JLabel                LBL_Sesso;
    private JLabel                LBL_Statistica2;       // ← aggiunto
    private JLabel                LBL_StatisticaUnica;
    private JLabel                LBL_Tipo;
    private JTextField            TXT_Eta;
    private JTextField            TXT_NMaglia;
    private JTextField            TXT_Nome;
    private JTextField            TXT_Sesso;
    private JTextField            TXT_Statistica2;       // ← aggiunto
    private JTextField            TXT_StatisticaUnica;
}
