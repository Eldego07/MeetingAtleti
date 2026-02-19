package meetingatleti;

/**
 * Rappresenta una singola voce della classifica di una gara.
 *
 * Incapsula tutti i dati necessari per visualizzare una riga in tabella:
 * posizione, atleta, punteggio, medaglia e flag di parità.
 *
 * Prodotta da {@link GestorePunteggio#calcolaClassifica(Gara)}.
 *
 * <pre>
 * Esempio:
 *   GestorePunteggio gp = new GestorePunteggio();
 *   ArrayList&lt;VocePunteggio&gt; classifica = gp.calcolaClassifica(gara);
 *   for (VocePunteggio v : classifica) {
 *       System.out.println(v.getMedaglia().getSimbolo() + " "
 *                        + v.getPosizione() + ". "
 *                        + v.getAtleta().getNome()
 *                        + "  →  " + v.getPunteggio() + " pt"
 *                        + (v.isPariMerito() ? " (=)" : ""));
 *   }
 * </pre>
 */
public class VocePunteggio {

    // ── campi ──────────────────────────────────────────────────────────────

    private final Atleta  atleta;
    private final int     posizione;   // 1-based, può essere uguale al precedente in caso di parità
    private final int     punteggio;
    private final Medaglia medaglia;
    private final boolean pariMerito;  // true se ha lo stesso punteggio dell'atleta precedente in classifica

    // ── costruttore ────────────────────────────────────────────────────────

    /**
     * @param atleta     atleta a cui appartiene la voce
     * @param posizione  posizione 1-based in classifica
     * @param punteggio  punteggio calcolato
     * @param pariMerito true se ha lo stesso punteggio del precedente
     */
    public VocePunteggio(Atleta atleta, int posizione, int punteggio, boolean pariMerito) {
        if (atleta == null) throw new IllegalArgumentException("atleta non può essere null");
        this.atleta     = atleta;
        this.posizione  = posizione;
        this.punteggio  = punteggio;
        this.pariMerito = pariMerito;
        this.medaglia   = Medaglia.fromPosizione(posizione);
    }

    // ── getter ─────────────────────────────────────────────────────────────

    /** L'atleta a cui appartiene questa voce. */
    public Atleta   getAtleta()     { return atleta; }

    /** Posizione in classifica (1 = primo, 2 = secondo, …). */
    public int      getPosizione()  { return posizione; }

    /** Punteggio numerico calcolato da {@link Atleta#calcolaPunteggio()}. */
    public int      getPunteggio()  { return punteggio; }

    /** Medaglia assegnata in base alla posizione. */
    public Medaglia getMedaglia()   { return medaglia; }

    /**
     * True se l'atleta ha esattamente lo stesso punteggio dell'atleta
     * che lo precede in classifica.
     */
    public boolean isPariMerito()   { return pariMerito; }

    // ── helper ─────────────────────────────────────────────────────────────

    /**
     * Restituisce la stringa della posizione con eventuale suffisso "=".
     * Es: "1", "2=", "2=".
     */
    public String posizioneLabel() {
        return posizione + (pariMerito ? "=" : "");
    }

    /**
     * Restituisce l'etichetta completa con simbolo medaglia.
     * Es: "🥇 1", "🥈 2=".
     */
    public String etichettaCompleta() {
        String sim = medaglia.getSimbolo();
        return (sim.isEmpty() ? "" : sim + " ") + posizioneLabel();
    }

    @Override
    public String toString() {
        return etichettaCompleta() + ". ["
                + atleta.getPettorale() + "] "
                + atleta.getNome()
                + "  →  " + punteggio + " pt"
                + (pariMerito ? " (pari merito)" : "");
    }
}
