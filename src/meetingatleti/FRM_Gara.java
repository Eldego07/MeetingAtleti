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

    // mappa indice riga → oggetto reale
    private final ArrayList<Gara>   indiceGare   = new ArrayList<>();
    private final ArrayList<Atleta> indiceAtleti = new ArrayList<>();  // per mappare LST_Atleti

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
        LST_Gare.addMouseListener(new GareMouseHandler());

        // tasto destro su LST_Atleti → menu contestuale "Aggiungi a gara"
        LST_Atleti.addMouseListener(new AtletiMouseHandler());

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
     * Ricarica LST_Atleti con:
     *   1. Atleti LIBERI filtrati per tipo e sesso, con prefisso [LIBERO]
     *   2. Atleti della gara selezionata (se c'è), filtrati per tipo e sesso
     *
     * Se una gara è selezionata i filtri vengono BLOCCATI sui suoi valori
     * (tipo e categoria sono già noti, non ha senso cambiarli).
     * Se nessuna gara è selezionata i filtri vengono sbloccati.
     */
    private void aggiornaListaAtleti() {
        modelAtleti.clear();
        indiceAtleti.clear();

        Gara sel = garaSelezionata();

        // ── blocca/sblocca filtri ─────────────────────────────────────────
        if (sel != null) {
            bloccaFiltriPerGara(sel);
        } else {
            sbloccaFiltri();
        }

        String filtroTipo = (String) CMB_TipoGara.getSelectedItem();
        boolean tuttiTipi = (filtroTipo == null || filtroTipo.isBlank()
                             || filtroTipo.trim().equalsIgnoreCase("Tutti"));

        // ── 1. Atleti LIBERI (filtrati per tipo e sesso) ──────────────────
        for (Atleta a : AppData.getInstance().getAtletiLiberi()) {
            if (!corrispondeSesso(a) || (!tuttiTipi && !corrispondeTipo(a, filtroTipo))) continue;
            modelAtleti.addElement("[LIBERO] " + a.toString());
            indiceAtleti.add(a);
        }

        // ── separatore visivo (solo se ci sono liberi e c'è anche una gara) ─
        if (!indiceAtleti.isEmpty() && sel != null) {
            modelAtleti.addElement("── atleti della gara ──────────────────");
            indiceAtleti.add(null);   // segnaposto: non selezionabile
        }

        // ── 2. Atleti della gara selezionata ──────────────────────────────
        if (sel == null) return;

        ArrayList<Atleta> dopoSesso;
        if (RBT_M.isSelected())      dopoSesso = sel.getAtletiM();
        else if (RBT_F.isSelected()) dopoSesso = sel.getAtletiF();
        else                         dopoSesso = sel.getAtleti();

        for (Atleta a : dopoSesso) {
            if (tuttiTipi || corrispondeTipo(a, filtroTipo)) {
                modelAtleti.addElement(a.toString());
                indiceAtleti.add(a);
            }
        }
    }

    /**
     * Blocca i filtri impostandoli sui valori della gara selezionata.
     *
     *   Sesso   → forzato sulla categoria della gara (M o F)
     *   Tipo    → Saltatore per Salto, Pesista per Lancio,
     *             "Tutti" per Corsa (ha 3 sotto-tipi validi)
     */
    private void bloccaFiltriPerGara(Gara g) {
        // sesso
        if ("M".equalsIgnoreCase(g.getCategoria())) RBT_M.setSelected(true);
        else                                         RBT_F.setSelected(true);
        RBT_M.setEnabled(false);
        RBT_F.setEnabled(false);

        // tipo
        if      (g.getTipoGaraSalto()  != null) CMB_TipoGara.setSelectedItem("Saltatore");
        else if (g.getTipoGaraLancio() != null) CMB_TipoGara.setSelectedItem("Pesista");
        else                                     CMB_TipoGara.setSelectedItem("Tutti");
        // per la corsa lasciamo "Tutti" selezionabile: dentro una gara di corsa
        // possono convivere Velocista / Fondometrista / Ostacolista
        CMB_TipoGara.setEnabled(g.getTipoGaraCorsa() != null);
    }

    /** Riabilita tutti i filtri quando nessuna gara è selezionata. */
    private void sbloccaFiltri() {
        RBT_M.setEnabled(true);
        RBT_F.setEnabled(true);
        CMB_TipoGara.setEnabled(true);
    }

    /**
     * Controlla se l'atleta corrisponde al filtro sesso attivo.
     * Se nessun radio è selezionato, tutti i sessi passano.
     */
    private boolean corrispondeSesso(Atleta a) {
        if (RBT_M.isSelected()) return "M".equalsIgnoreCase(a.getSesso());
        if (RBT_F.isSelected()) return "F".equalsIgnoreCase(a.getSesso());
        return true;
    }

    /**
     * Controlla se l'atleta corrisponde al tipo selezionato nel combo.
     * Usato SOLO per il filtro di visualizzazione in LST_Atleti.
     */
    private boolean corrispondeTipo(Atleta a, String tipoFiltro) {
        switch (tipoFiltro.trim()) {
            case "Velocista":
                return (a instanceof Velocisti) && ((Velocisti) a).isVelocista();
            case "Pesista":
                return a instanceof Lanciatori;
            case "Saltatore":
                return a instanceof Saltatori;
            case "Fondometrista":
                return (a instanceof Velocisti) && ((Velocisti) a).isFondometrista();
            case "Ostacolista":
                return (a instanceof Velocisti) && ((Velocisti) a).isOstacolista();
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

    /** Ritorna l'Atleta corrispondente alla riga selezionata in LST_Atleti. */
    private Atleta atleteSelezionato() {
        int idx = LST_Atleti.getSelectedIndex();
        if (idx < 0 || idx >= indiceAtleti.size()) return null;
        return indiceAtleti.get(idx);
    }

    /** @return true se l'atleta selezionato è un atleta libero (non ancora in una gara). */
    private boolean isAtletaLibero(Atleta a) {
        return a != null && AppData.getInstance().getAtletiLiberi().contains(a);
    }

    // ── menu contestuale su LST_Gare ───────────────────────────────────────

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

    // ── menu contestuale su LST_Atleti ─────────────────────────────────────

    /** Mostra menu contestuale su LST_Atleti (tasto destro). */
    private void mostraMenuAtleta(java.awt.event.MouseEvent e) {
        Atleta atleta = atleteSelezionato();
        if (atleta == null) return;  // null = riga separatore o nessuna selezione

        JPopupMenu menu = new JPopupMenu();

        if (isAtletaLibero(atleta)) {
            Gara garaTarget = garaSelezionata();
            if (garaTarget != null) {
                // controlla compatibilità preventiva per mostrare messaggio chiaro
                String incompatibilita = verificaCompatibilita(atleta, garaTarget);

                JMenuItem itemAggiungi = new JMenuItem("➕  Aggiungi a gara selezionata");
                if (incompatibilita == null) {
                    itemAggiungi.addActionListener(a -> aggiungiAtletaAGara(atleta));
                } else {
                    itemAggiungi.setEnabled(false);
                    itemAggiungi.setToolTipText(incompatibilita);
                }
                menu.add(itemAggiungi);

                if (incompatibilita != null) {
                    JMenuItem itemInfo = new JMenuItem("⚠  " + incompatibilita);
                    itemInfo.setEnabled(false);
                    menu.add(itemInfo);
                }
            } else {
                JMenuItem itemNessuna = new JMenuItem("⚠  Seleziona prima una gara in lista");
                itemNessuna.setEnabled(false);
                menu.add(itemNessuna);
            }
        }

        if (menu.getComponentCount() > 0) {
            menu.show(LST_Atleti, e.getX(), e.getY());
        }
    }

    /**
     * Verifica se l'atleta è compatibile con la gara.
     * @return null se compatibile, stringa con il motivo altrimenti.
     */
    private String verificaCompatibilita(Atleta a, Gara gara) {
        if (!a.getSesso().equalsIgnoreCase(gara.getCategoria()))
            return "Sesso '" + a.getSesso() + "' ≠ categoria gara '" + gara.getCategoria() + "'";
        if (gara.getTipoGaraCorsa()  != null && !(a instanceof Velocisti))
            return "Gara di CORSA: richiede Velocista / Fondometrista / Ostacolista";
        if (gara.getTipoGaraSalto()  != null && !(a instanceof Saltatori))
            return "Gara di SALTO: richiede Saltatore";
        if (gara.getTipoGaraLancio() != null && !(a instanceof Lanciatori))
            return "Gara di LANCIO: richiede Pesista";
        for (Atleta x : gara.getAtleti())
            if (x.getPettorale().equals(a.getPettorale()))
                return "Pettorale " + a.getPettorale() + " già usato in questa gara";
        return null;
    }

    /**
     * Aggiunge un atleta libero alla gara selezionata.
     * Controlla compatibilità tipo/sesso, rimuove da atletiLiberi se successo.
     */
    private void aggiungiAtletaAGara(Atleta atleta) {
        Gara gara = garaSelezionata();
        if (gara == null || atleta == null) return;

        // Recupera la Prestazione in attesa salvata da FRM_Atleti
        Prestazione prestazione = AppData.getInstance().getPrestazioneInAttesa(atleta);
        if (prestazione == null) {
            JOptionPane.showMessageDialog(this,
                    "Impossibile aggiungere l'atleta: nessuna prestazione associata.\n"
                    + "Crea l'atleta nuovamente da FRM_Atleti con una gara attiva.",
                    "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }
        boolean ok = gara.iscrizione(atleta, prestazione);
        if (ok) {
            AppData.getInstance().rimuoviAtletaLibero(atleta);  // rimuove anche prestazione in attesa
            JOptionPane.showMessageDialog(this,
                    "✔  Atleta aggiunto alla gara con successo!\n" + atleta,
                    "OK", JOptionPane.INFORMATION_MESSAGE);
            aggiornaListaAtleti();
            aggiornaListaGare();  // aggiorna conteggio partecipanti
        } else {
            String motivoTipo = "";
            if (gara.getTipoGaraCorsa() != null && !(atleta instanceof Velocisti))
                motivoTipo = "• Gara di CORSA: accetta solo Velocista / Fondometrista / Ostacolista\n";
            else if (gara.getTipoGaraSalto() != null && !(atleta instanceof Saltatori))
                motivoTipo = "• Gara di SALTO: accetta solo Saltatore\n";
            else if (gara.getTipoGaraLancio() != null && !(atleta instanceof Lanciatori))
                motivoTipo = "• Gara di LANCIO: accetta solo Pesista\n";

            JOptionPane.showMessageDialog(this,
                    "Impossibile aggiungere l'atleta.\n"
                    + motivoTipo
                    + "• Numero maglia " + atleta.getPettorale() + " già usato, oppure\n"
                    + "• Sesso '" + atleta.getSesso() + "' ≠ categoria '" + gara.getCategoria() + "'",
                    "Errore", JOptionPane.ERROR_MESSAGE);
        }
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

        // ── usiamo String[] per tutti i dialog, poi valueOf() per convertire ──
        // Questo evita di passare array di enum a JOptionPane (classloading issue
        // in NetBeans quando il .form è presente nello stesso progetto)
        switch (sceltaTipo) {
            case 0: { // Corsa
                String[] valori = {"centom","duecentom","quattrocentom","ottocentom",
                        "millecinquecentom","cinquemilam","diecimilam",
                        "Ostacoli","quattrocentoOstacoli","tremilaSiepi","Maratona","Maradona"};
                String scelta = (String) JOptionPane.showInputDialog(
                        this, "Specialità corsa:", "Nuova Gara",
                        JOptionPane.PLAIN_MESSAGE, null, valori, valori[0]);
                if (scelta == null) return;
                nuova.setTipoGaraCorsa(TipoGaraCorsa.valueOf(scelta));
                break;
            }
            case 1: { // Salto
                String[] valori = {"Alto","Lungo","Asta","Triplo"};
                String scelta = (String) JOptionPane.showInputDialog(
                        this, "Specialità salto:", "Nuova Gara",
                        JOptionPane.PLAIN_MESSAGE, null, valori, valori[0]);
                if (scelta == null) return;
                nuova.setTipoGaraSalto(TipoGaraSalto.valueOf(scelta));
                break;
            }
            case 2: { // Lancio
                String[] valori = {"Peso","Disco","Martello","Giavellotto"};
                String scelta = (String) JOptionPane.showInputDialog(
                        this, "Specialità lancio:", "Nuova Gara",
                        JOptionPane.PLAIN_MESSAGE, null, valori, valori[0]);
                if (scelta == null) return;
                nuova.setTipoGaraLancio(TipoGaraLancio.valueOf(scelta));
                break;
            }
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
        // NON blocchiamo più se sel == null: permette di creare atleti liberi
        AppData.getInstance().setGaraCorrente(sel);

        FRM_Atleti formAtleti = new FRM_Atleti();
        formAtleti.setVisible(true);
        // quando FRM_Atleti si chiude, aggiorna le liste
        formAtleti.addWindowListener(new AtletiWindowHandler());
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
                new String[]{"Tutti", "Velocista", "Fondometrista", "Ostacolista", "Saltatore", "Pesista"}));
        CMB_TipoGara.setSelectedIndex(0); // default = "Tutti"
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

    // ── classi interne nominate (evitano FRM_Gara$1 che NetBeans non trova) ──

    /** Gestisce i click sulla lista gare (sinistro = deseleziona se già selezionata, destro = menu). */
    private class GareMouseHandler extends java.awt.event.MouseAdapter {
        @Override
        public void mousePressed(java.awt.event.MouseEvent e) {
            int idx = LST_Gare.locationToIndex(e.getPoint());

            if (SwingUtilities.isRightMouseButton(e)) {
                if (idx >= 0) LST_Gare.setSelectedIndex(idx);
                mostraMenuGara(e);
            } else if (SwingUtilities.isLeftMouseButton(e)) {
                // se il click è sulla riga già selezionata → deseleziona
                if (idx >= 0 && idx == LST_Gare.getSelectedIndex()) {
                    LST_Gare.clearSelection();
                    AppData.getInstance().setGaraCorrente(null);
                    aggiornaListaAtleti();
                }
            }
        }
    }

    /** Gestisce il tasto destro sulla lista atleti (per aggiungere atleti liberi). */
    private class AtletiMouseHandler extends java.awt.event.MouseAdapter {
        @Override
        public void mousePressed(java.awt.event.MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
                int idx = LST_Atleti.locationToIndex(e.getPoint());
                if (idx >= 0) LST_Atleti.setSelectedIndex(idx);
                mostraMenuAtleta(e);
            }
        }
    }

    /** Aggiorna le liste quando FRM_Atleti viene chiuso. */
    private class AtletiWindowHandler extends java.awt.event.WindowAdapter {
        @Override
        public void windowClosed(java.awt.event.WindowEvent e) {
            aggiornaListaAtleti();
            aggiornaListaGare();
        }
    }

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
