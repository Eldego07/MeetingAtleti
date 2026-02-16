package meetingatleti;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/**
 * Form 1 di 3 – Inserimento atleti.
 *
 * Componenti (stessi nomi del .form):
 *   LBL_Nome, TXT_Nome
 *   LBL_Sesso, TXT_Sesso
 *   LBL_Eta, TXT_Eta
 *   LBL_Tipo, CMB_Tipo  (Velocista | Pesista | Saltatore | Fondometrista)
 *   LBL_NMaglia, TXT_NMaglia     ← pettorale
 *   LBL_StatisticaUnica, TXT_StatisticaUnica  ← campo dinamico per tipo
 *   BTN_Inserisci
 *   BTN_Sinistra  ← disabilitato (siamo al primo form)
 *   BTN_Destra    ← apre FRM_Classifica (Form 2)
 */
public class FRM_Atleti extends javax.swing.JFrame {

    private static final Logger logger = Logger.getLogger(FRM_Atleti.class.getName());

    public FRM_Atleti() {
        initComponents();
        // titolo con la gara corrente
        Gara g = AppData.getInstance().getGaraCorrente();
        if (g != null)
            setTitle("Inserimento Atleti  [1/3]  →  " + g.getNomeGara() + "  [" + g.getCategoria() + "]");
        // popola CMB_Tipo solo con i tipi compatibili con la gara
        aggiornaCMBTipo();
        aggiornaLabelStatistica();
    }

    /**
     * Popola CMB_Tipo con i soli tipi di atleta ammessi dalla gara corrente.
     *   Corsa  → Velocista, Fondometrista, Ostacolista  (tutti Velocisti internamente)
     *   Salto  → Saltatore
     *   Lancio → Pesista
     *   Nessuna gara / generica → tutti i tipi
     */
    private void aggiornaCMBTipo() {
        Gara g = AppData.getInstance().getGaraCorrente();
        CMB_Tipo.removeAllItems();
        if (g == null) {
            // nessuna gara: mostra tutti
            CMB_Tipo.addItem("Velocista");
            CMB_Tipo.addItem("Fondometrista");
            CMB_Tipo.addItem("Ostacolista");
            CMB_Tipo.addItem("Pesista");
            CMB_Tipo.addItem("Saltatore");
        } else if (g.getTipoGaraCorsa() != null) {
            // gara di corsa: solo i corridori
            CMB_Tipo.addItem("Velocista");
            CMB_Tipo.addItem("Fondometrista");
            CMB_Tipo.addItem("Ostacolista");
        } else if (g.getTipoGaraSalto() != null) {
            // gara di salto: solo saltatori
            CMB_Tipo.addItem("Saltatore");
        } else if (g.getTipoGaraLancio() != null) {
            // gara di lancio: solo pesisti
            CMB_Tipo.addItem("Pesista");
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  LOGICA APPLICATIVA
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Aggiorna LBL_StatisticaUnica in base al tipo selezionato nel combo.
     *
     *   Velocista     → "Tempo gara (s)"  [+ reazione 15cs default]
     *   Fondometrista → "Tempo gara (s)"  [nessuna reazione]
     *   Ostacolista   → "Tempo gara (s)"  [+ penalità cs]
     *   Pesista       → "Lancio (cm)"
     *   Saltatore     → "Salto (cm)"
     */
    private void aggiornaLabelStatistica() {
        String tipo = (String) CMB_Tipo.getSelectedItem();
        if (tipo == null) return;
        switch (tipo) {
            case "Velocista":
                LBL_StatisticaUnica.setText("Tempo gara (s)");
                break;
            case "Fondometrista":
                LBL_StatisticaUnica.setText("Tempo gara (s)");
                break;
            case "Ostacolista":
                LBL_StatisticaUnica.setText("Tempo gara (s)");
                break;
            case "Pesista":
                LBL_StatisticaUnica.setText("Lancio (cm)");
                break;
            case "Saltatore":
                LBL_StatisticaUnica.setText("Salto (cm)");
                break;
        }
    }

    /** Legge i campi, crea l'atleta e lo iscrive alla gara. */
    private void inserisciAtleta() {
        Gara gara = AppData.getInstance().getGaraCorrente();
        if (gara == null) {
            errore("Nessuna gara selezionata!\nTorna su FRM_Gara e premi Avvia.");
            return;
        }

        String nome  = TXT_Nome.getText().trim();
        String sesso = TXT_Sesso.getText().trim().toUpperCase();
        String tipo  = (String) CMB_Tipo.getSelectedItem();

        if (nome.isEmpty()) { errore("Inserisci il nome."); return; }
        if (!sesso.equals("M") && !sesso.equals("F")) {
            errore("Sesso deve essere M oppure F."); return;
        }

        int eta, nMaglia, statistica;
        try { eta = Integer.parseInt(TXT_Eta.getText().trim()); }
        catch (NumberFormatException e) { errore("Età non valida."); return; }

        try { nMaglia = Integer.parseInt(TXT_NMaglia.getText().trim()); }
        catch (NumberFormatException e) { errore("Numero maglia non valido."); return; }

        try {
            statistica = Integer.parseInt(TXT_StatisticaUnica.getText().trim());
            if (statistica <= 0) throw new NumberFormatException();
        }
        catch (NumberFormatException e) { errore("Statistica non valida (numero intero > 0)."); return; }

        // crea atleta del tipo corretto
        Atleta atleta;
        switch (tipo) {
            case "Velocista": {
                // velocista: inserisce il tempo gara; reazione default 15cs (0.15s)
                Velocisti v = new Velocisti(nome, sesso, eta, nMaglia);
                v.setTempoGara((double) statistica);   // statistica = tempo in secondi (intero)
                v.setTempoReazione(15);                // reazione standard da sparo: 0.15s
                v.setTempoOstacolo(0);
                atleta = v;
                break;
            }
            case "Fondometrista": {
                // fondometrista: solo tempo gara, nessuna reazione
                Velocisti f = new Velocisti(nome, sesso, eta, nMaglia);
                f.setTempoGara((double) statistica);
                f.setTempoReazione(0);
                f.setTempoOstacolo(0);
                atleta = f;
                break;
            }
            case "Ostacolista": {
                // ostacolista: tempo gara, penalità default 0cs (nessun ostacolo abbattuto)
                Velocisti o = new Velocisti(nome, sesso, eta, nMaglia);
                o.setTempoGara((double) statistica);
                o.setTempoReazione(0);
                o.setTempoOstacolo(0);   // 0 = nessun ostacolo abbattuto
                atleta = o;
                break;
            }
            case "Pesista": {
                Lanciatori l = new Lanciatori(nome, sesso, eta, nMaglia);
                l.setDistanzaLancio(statistica);
                atleta = l;
                break;
            }
            case "Saltatore": {
                Saltatori s = new Saltatori(nome, sesso, eta, nMaglia);
                s.setDistanzaSalto(statistica);
                atleta = s;
                break;
            }
            default:
                errore("Tipo atleta non riconosciuto."); return;
        }

        boolean ok = gara.iscrizione(atleta);
        if (ok) {
            JOptionPane.showMessageDialog(this,
                    "✔  Iscritto con successo!\n" + atleta,
                    "OK", JOptionPane.INFORMATION_MESSAGE);
            pulisciCampi();
        } else {
            // Determina il motivo specifico del rifiuto
            String motivoTipo = "";
            if (gara.getTipoGaraCorsa() != null && !(atleta instanceof Velocisti))
                motivoTipo = "• Gara di CORSA: accetta solo Velocista / Fondometrista / Ostacolista\n";
            else if (gara.getTipoGaraSalto() != null && !(atleta instanceof Saltatori))
                motivoTipo = "• Gara di SALTO: accetta solo Saltatore\n";
            else if (gara.getTipoGaraLancio() != null && !(atleta instanceof Lanciatori))
                motivoTipo = "• Gara di LANCIO: accetta solo Pesista\n";

            errore("Iscrizione fallita.\n"
                    + motivoTipo
                    + "• Numero maglia " + nMaglia + " già usato, oppure\n"
                    + "• Sesso '" + sesso + "' ≠ categoria '" + gara.getCategoria() + "'");
        }
    }

    // ── Navigazione form ───────────────────────────────────────────────────

    /** ◀  Disabilitato: siamo al primo form. */
    // BTN_Sinistra è disabled in initComponents.

    /** ▶  Apre FRM_Classifica (Form 2) e chiude questo. */
    private void goDestra() {
        new FRM_Classifica().setVisible(true);
        this.dispose();
    }

    private void aggiornaCursore() {
        // mantenuto per compatibilità – non più usato per la navigazione
    }

    private void pulisciCampi() {
        TXT_Nome.setText("");
        TXT_Sesso.setText("");
        TXT_Eta.setText("");
        TXT_NMaglia.setText("");
        TXT_StatisticaUnica.setText("");
    }

    private void errore(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Errore", JOptionPane.ERROR_MESSAGE);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  CODICE GENERATO DA NETBEANS  (initComponents)
    //  Variabili identiche al .form – NON modificare i nomi
    // ══════════════════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private void initComponents() {

        LBL_Nome           = new javax.swing.JLabel();
        TXT_Nome           = new javax.swing.JTextField();
        TXT_Sesso          = new javax.swing.JTextField();
        LBL_Sesso          = new javax.swing.JLabel();
        TXT_Eta            = new javax.swing.JTextField();
        LBL_Eta            = new javax.swing.JLabel();
        LBL_Tipo           = new javax.swing.JLabel();
        CMB_Tipo           = new javax.swing.JComboBox<>();
        BTN_Inserisci      = new javax.swing.JButton();
        BTN_Destra         = new javax.swing.JButton();
        BTN_Sinistra       = new javax.swing.JButton();
        LBL_NMaglia        = new javax.swing.JLabel();
        TXT_NMaglia        = new javax.swing.JTextField();
        TXT_StatisticaUnica= new javax.swing.JTextField();
        LBL_StatisticaUnica= new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Iscrizione Atleti");

        LBL_Nome.setText("Nome");
        LBL_Sesso.setText("Sesso");
        LBL_Eta.setText("Età");
        LBL_Tipo.setText("Tipo");
        LBL_NMaglia.setText("nMaglia");
        LBL_StatisticaUnica.setText("Statistica Unica");

        CMB_Tipo.setModel(new javax.swing.DefaultComboBoxModel<>(
                new String[]{"Velocista", "Pesista", "Saltatore", "Fondometrista"}));
        CMB_Tipo.addActionListener(evt -> CMB_TipoActionPerformed(evt));

        BTN_Inserisci.setText("Inserisci");
        BTN_Inserisci.addActionListener(evt -> inserisciAtleta());

        BTN_Destra.setText(">");
        BTN_Destra.setToolTipText("Vai a Classifica Gara (Form 2)");
        BTN_Destra.addActionListener(evt -> goDestra());

        BTN_Sinistra.setText("<");
        BTN_Sinistra.setEnabled(false); // primo form, non si può tornare indietro
        BTN_Sinistra.setToolTipText("Sei già al primo form");

        TXT_NMaglia.addActionListener(evt -> TXT_NMagliaActionPerformed(evt));

        // ── layout (GroupLayout identico al .form) ────────────────────────
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);

        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(148, 148, Short.MAX_VALUE)
                .addComponent(BTN_Sinistra)
                .addGap(57, 57, 57)
                .addComponent(BTN_Destra)
                .addGap(149, 149, 149))
            .addGroup(layout.createSequentialGroup()
                .addGap(46, 46, 46)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(LBL_Nome,            javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(LBL_Sesso,           javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(LBL_Eta,             javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(LBL_Tipo,            javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(LBL_NMaglia)
                    .addComponent(LBL_StatisticaUnica))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(TXT_Nome,            javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TXT_Sesso,           javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(CMB_Tipo,            javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(TXT_NMaglia,     javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(TXT_Eta,         javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 71, Short.MAX_VALUE))
                    .addComponent(BTN_Inserisci,       javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(TXT_StatisticaUnica, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(42, 42, 42))
        );

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(LBL_Nome)
                    .addComponent(TXT_Nome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(LBL_Sesso)
                            .addComponent(TXT_Sesso, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(LBL_Eta)
                        .addGap(13, 13, 13)
                        .addComponent(LBL_NMaglia)
                        .addGap(12, 12, 12))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(40, 40, 40)
                        .addComponent(TXT_Eta,         javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(TXT_NMaglia,     javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(LBL_Tipo)
                        .addGap(6, 6, 6))
                    .addComponent(CMB_Tipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(LBL_StatisticaUnica)
                    .addComponent(TXT_StatisticaUnica, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(BTN_Inserisci)
                .addGap(29, 29, 29)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(BTN_Destra)
                    .addComponent(BTN_Sinistra))
                .addGap(15, 15, 15))
        );

        pack();
        setLocationRelativeTo(null);
    }

    // ── handler generati dal .form ─────────────────────────────────────────

    private void CMB_TipoActionPerformed(java.awt.event.ActionEvent evt) {
        aggiornaLabelStatistica();
    }

    private void TXT_NMagliaActionPerformed(java.awt.event.ActionEvent evt) {
        // invocato premendo ENTER nel campo nMaglia
    }

    // ── dichiarazione variabili (stessi nomi del .form) ────────────────────

    private javax.swing.JButton      BTN_Destra;
    private javax.swing.JButton      BTN_Inserisci;
    private javax.swing.JButton      BTN_Sinistra;
    private javax.swing.JComboBox<String> CMB_Tipo;
    private javax.swing.JLabel       LBL_Eta;
    private javax.swing.JLabel       LBL_NMaglia;
    private javax.swing.JLabel       LBL_Nome;
    private javax.swing.JLabel       LBL_Sesso;
    private javax.swing.JLabel       LBL_StatisticaUnica;
    private javax.swing.JLabel       LBL_Tipo;
    private javax.swing.JTextField   TXT_Eta;
    private javax.swing.JTextField   TXT_NMaglia;
    private javax.swing.JTextField   TXT_Nome;
    private javax.swing.JTextField   TXT_Sesso;
    private javax.swing.JTextField   TXT_StatisticaUnica;
}
