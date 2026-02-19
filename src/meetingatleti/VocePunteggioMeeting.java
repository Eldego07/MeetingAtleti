package meetingatleti;

import java.util.ArrayList;

/**
 * Rappresenta una singola voce della classifica generale del meeting.
 *
 * Aggrega i risultati di un atleta su tutte le gare a cui ha partecipato,
 * calcolando il totale dei punti e la media.
 *
 * Prodotta da {@link GestorePunteggio#calcolaClassificaMeeting(Meeting)}.
 *
 * <p>Nota: l'identità dell'atleta è determinata dalla coppia
 * (nome, pettorale) — un atleta può partecipare a più gare con
 * lo stesso pettorale, i cui punteggi vengono sommati.</p>
 */
public class VocePunteggioMeeting {

    // ── campi ──────────────────────────────────────────────────────────────

    private final String           nomeAtleta;
    private final int              pettorale;
    private final String           sesso;
    private int                    totalePunti;
    private int                    posizione;    // settata da GestorePunteggio dopo il sort
    private Medaglia               medaglia;     // settata da GestorePunteggio
    private boolean                pariMerito;
    private final ArrayList<String> garePartecipate;  // nomi delle gare
    private final ArrayList<Integer> puntiPerGara;    // punteggio in ogni gara

    // ── costruttore ────────────────────────────────────────────────────────

    /**
     * @param nomeAtleta nome dell'atleta
     * @param pettorale  numero pettorale
     * @param sesso      "M" o "F"
     */
    public VocePunteggioMeeting(String nomeAtleta, int pettorale, String sesso) {
        this.nomeAtleta      = nomeAtleta;
        this.pettorale       = pettorale;
        this.sesso           = sesso;
        this.totalePunti     = 0;
        this.posizione       = 0;
        this.medaglia        = Medaglia.NESSUNA;
        this.pariMerito      = false;
        this.garePartecipate = new ArrayList<>();
        this.puntiPerGara    = new ArrayList<>();
    }

    // ── accumulo punteggi ──────────────────────────────────────────────────

    /**
     * Aggiunge il risultato di una gara al totale.
     *
     * @param nomeGara   nome della gara
     * @param punti      punteggio ottenuto in quella gara
     */
    public void aggiungiGara(String nomeGara, int punti) {
        garePartecipate.add(nomeGara);
        puntiPerGara.add(punti);
        totalePunti += punti;
    }

    // ── getter ─────────────────────────────────────────────────────────────

    public String           getNomeAtleta()       { return nomeAtleta; }
    public int              getPettorale()         { return pettorale; }
    public String           getSesso()             { return sesso; }
    public int              getTotalePunti()       { return totalePunti; }
    public int              getNumeroGare()        { return garePartecipate.size(); }
    public ArrayList<String>  getGarePartecipate() { return garePartecipate; }
    public ArrayList<Integer> getPuntiPerGara()    { return puntiPerGara; }

    public int      getPosizione()                 { return posizione; }
    public Medaglia getMedaglia()                  { return medaglia; }
    public boolean  isPariMerito()                 { return pariMerito; }

    // ── setter (usati da GestorePunteggio dopo il sort) ───────────────────

    public void setPosizione(int p)       { this.posizione  = p; this.medaglia = Medaglia.fromPosizione(p); }
    public void setPariMerito(boolean b)  { this.pariMerito = b; }

    // ── statistiche ────────────────────────────────────────────────────────

    /**
     * Punteggio medio nelle gare disputate.
     * @return media, oppure 0.0 se nessuna gara
     */
    public double punteggioMedio() {
        if (puntiPerGara.isEmpty()) return 0.0;
        int somma = 0;
        for (int p : puntiPerGara) somma += p;
        return (double) somma / puntiPerGara.size();
    }

    /**
     * Punteggio massimo tra tutte le gare disputate.
     * @return massimo, oppure 0 se nessuna gara
     */
    public int punteggioMassimo() {
        if (puntiPerGara.isEmpty()) return 0;
        int max = Integer.MIN_VALUE;
        for (int p : puntiPerGara) if (p > max) max = p;
        return max;
    }

    /**
     * Etichetta di posizione con eventuale "=" per pari merito.
     */
    public String posizioneLabel() {
        return posizione + (pariMerito ? "=" : "");
    }

    @Override
    public String toString() {
        return medaglia.getSimbolo() + " " + posizioneLabel() + ". "
                + "[" + pettorale + "] " + nomeAtleta + " (" + sesso + ")"
                + "  →  " + totalePunti + " pt su " + getNumeroGare() + " gare";
    }
}
