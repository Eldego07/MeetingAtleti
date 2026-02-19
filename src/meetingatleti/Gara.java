package meetingatleti;

import java.util.ArrayList;

/**
 * Rappresenta una singola gara del meeting.
 *
 * v3 – supporto multi-disciplina:
 * L'iscrizione accetta ora un oggetto Atleta e una Prestazione separati.
 * La compatibilità viene verificata sul tipo della Prestazione (non sul tipo
 * dell'atleta con instanceof). Lo stesso oggetto Atleta può quindi iscriversi
 * a gare di tipo diverso con lo stesso numero di maglia.
 *
 * Controlli di iscrizione:
 *   1. Atleta e prestazione non null
 *   2. Pettorale non già presente IN QUESTA gara
 *   3. Sesso compatibile con la categoria della gara
 *   4. Tipo della prestazione compatibile con il tipo di gara
 */
public class Gara implements Maschile, Femminile {

    public Gara() {
        atleti           = new ArrayList<>();
        atletiClassifica = new ArrayList<>();
        classificaVoci   = new ArrayList<>();
    }

    public Gara(String nomeGara, String categoria) {
        this();
        this.nomeGara  = nomeGara;
        this.categoria = categoria;
    }

    private String            nomeGara;
    private String            categoria;
    private ArrayList<Atleta> atleti;
    private ArrayList<Atleta> atletiClassifica;
    private ArrayList<VocePunteggio> classificaVoci;

    private TipoGaraSalto  tipoGaraSalto;
    private TipoGaraCorsa  tipoGaraCorsa;
    private TipoGaraLancio tipoGaraLancio;

    public String  getNomeGara()                     { return nomeGara; }
    public void    setNomeGara(String n)             { this.nomeGara = n; }
    public String  getCategoria()                    { return categoria; }
    public void    setCategoria(String c)            { this.categoria = c; }
    public ArrayList<Atleta> getAtleti()             { return atleti; }
    public TipoGaraSalto  getTipoGaraSalto()         { return tipoGaraSalto; }
    public void setTipoGaraSalto(TipoGaraSalto t)    { tipoGaraSalto  = t; tipoGaraCorsa = null; tipoGaraLancio = null; }
    public TipoGaraCorsa  getTipoGaraCorsa()         { return tipoGaraCorsa; }
    public void setTipoGaraCorsa(TipoGaraCorsa t)    { tipoGaraCorsa  = t; tipoGaraSalto = null; tipoGaraLancio = null; }
    public TipoGaraLancio getTipoGaraLancio()        { return tipoGaraLancio; }
    public void setTipoGaraLancio(TipoGaraLancio t)  { tipoGaraLancio = t; tipoGaraSalto = null; tipoGaraCorsa = null; }
    public int getNumeroPartecipanti()               { return atleti.size(); }

    public String getTipoDescrizione() {
        if (tipoGaraCorsa  != null) return tipoGaraCorsa.name();
        if (tipoGaraSalto  != null) return tipoGaraSalto.name();
        if (tipoGaraLancio != null) return tipoGaraLancio.name();
        return "N/A";
    }

    @Override
    public ArrayList<Atleta> getAtletiM() {
        ArrayList<Atleta> lista = new ArrayList<>();
        for (Atleta a : atleti) if ("M".equalsIgnoreCase(a.getSesso())) lista.add(a);
        return lista;
    }

    @Override
    public ArrayList<Atleta> getAtletiF() {
        ArrayList<Atleta> lista = new ArrayList<>();
        for (Atleta a : atleti) if ("F".equalsIgnoreCase(a.getSesso())) lista.add(a);
        return lista;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ISCRIZIONE v3 – Atleta + Prestazione separati
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Iscrive un atleta alla gara con la prestazione fornita.
     *
     * Un atleta con lo stesso pettorale può comparire in più gare dello stesso
     * meeting (multi-disciplina): il controllo pettorale è locale a questa gara.
     *
     * @param atleta      l'atleta (non null)
     * @param prestazione la prestazione per questa gara (non null)
     * @return true se iscrizione avvenuta con successo
     */
    public boolean iscrizione(Atleta atleta, Prestazione prestazione) {
        if (atleta == null || prestazione == null) return false;

        // 1. pettorale già presente IN QUESTA gara
        for (Atleta x : atleti)
            if (x.getPettorale().equals(atleta.getPettorale())) return false;

        // 2. sesso / categoria
        if (!atleta.getSesso().equalsIgnoreCase(categoria)) return false;

        // 3. tipo prestazione compatibile con tipo gara
        if (!tipoCompatibile(prestazione)) return false;

        atleta.aggiungiPrestazione(this, prestazione);
        atleti.add(atleta);
        invalidaClassifica();
        return true;
    }

    /**
     * Verifica la compatibilità tipo gara / tipo prestazione.
     *
     *  Gara Corsa  → VELOCISTA, FONDOMETRISTA, OSTACOLISTA
     *  Gara Salto  → SALTO
     *  Gara Lancio → LANCIO
     *  (nessun tipo) → qualsiasi
     */
    private boolean tipoCompatibile(Prestazione p) {
        if (tipoGaraCorsa  != null) return p.compatibileCorsa();
        if (tipoGaraSalto  != null) return p.compatibileSalto();
        if (tipoGaraLancio != null) return p.compatibileLancio();
        return true;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  CLASSIFICA (via GestorePunteggio)
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Ricalcola la classifica.
     * Imposta temporaneamente questa gara come garaCorrente in AppData
     * in modo che Atleta.calcolaPunteggio() (senza argomenti) usi la
     * prestazione corretta.
     */
    public void calcolaClassifica() {
        Gara precedente = AppData.getInstance().getGaraCorrente();
        AppData.getInstance().setGaraCorrente(this);

        classificaVoci   = GestorePunteggio.calcolaClassifica(this);
        atletiClassifica = new ArrayList<>();
        for (VocePunteggio v : classificaVoci)
            atletiClassifica.add(v.getAtleta());

        AppData.getInstance().setGaraCorrente(precedente);
    }

    public ArrayList<Atleta> getClassifica() {
        if (atletiClassifica.isEmpty() && !atleti.isEmpty()) calcolaClassifica();
        return atletiClassifica;
    }

    public ArrayList<VocePunteggio> getClassificaVoci() {
        if (classificaVoci.isEmpty() && !atleti.isEmpty()) calcolaClassifica();
        return classificaVoci;
    }

    public Atleta trovaVincitore() {
        if (atleti.isEmpty()) return null;
        ArrayList<Atleta> cl = getClassifica();
        return cl.isEmpty() ? null : cl.get(0);
    }

    public VocePunteggio trovaVincitoreVoce() {
        ArrayList<VocePunteggio> cl = getClassificaVoci();
        return cl.isEmpty() ? null : cl.get(0);
    }

    // ── statistiche ────────────────────────────────────────────────────────

    public int    getPunteggioMassimo()              { return GestorePunteggio.punteggioMassimo(this); }
    public int    getPunteggioMinimo()               { return GestorePunteggio.punteggioMinimo(this); }
    public double getPunteggioMedio()                { return GestorePunteggio.punteggioMedio(this); }
    public ArrayList<Atleta> getAtletiPariMerito()   { return GestorePunteggio.trovaPariMeritoPrimo(this); }
    public String getRiepilogoStatistiche()           { return GestorePunteggio.riepilogoStatistiche(this); }

    private void invalidaClassifica() {
        atletiClassifica.clear();
        classificaVoci.clear();
    }

    @Override
    public String toString() {
        return nomeGara + " [" + categoria + "] – " + getTipoDescrizione()
               + " (" + atleti.size() + " atleti)";
    }
}
