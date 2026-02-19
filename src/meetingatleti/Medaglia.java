package meetingatleti;

/**
 * Enum che rappresenta il tipo di medaglia assegnato a un atleta in base
 * alla posizione in classifica.
 *
 * Ogni valore espone:
 *   - simbolo   : stringa unicode usata nelle tabelle Swing
 *   - soglia    : posizione massima per cui la medaglia è assegnata
 *                 (es. ORO → solo posizione 1, BRONZO → fino a posizione 3)
 *
 * Utilizzo tipico:
 * <pre>
 *   Medaglia m = Medaglia.fromPosizione(2);   // → ARGENTO
 *   System.out.println(m.getSimbolo());       // → "🥈"
 * </pre>
 */
public enum Medaglia {

    ORO    ("🥇", 1),
    ARGENTO("🥈", 2),
    BRONZO ("🥉", 3),
    NESSUNA("",   Integer.MAX_VALUE);

    // ── campi ──────────────────────────────────────────────────────────────

    private final String simbolo;
    private final int    soglia;   // posizione massima (inclusiva) per cui vale

    // ── costruttore ────────────────────────────────────────────────────────

    Medaglia(String simbolo, int soglia) {
        this.simbolo = simbolo;
        this.soglia  = soglia;
    }

    // ── getter ─────────────────────────────────────────────────────────────

    /** Simbolo unicode, es. "🥇". Stringa vuota per NESSUNA. */
    public String getSimbolo() { return simbolo; }

    /** Soglia di posizione (1 per ORO, 2 per ARGENTO, 3 per BRONZO). */
    public int getSoglia()     { return soglia; }

    // ── factory ────────────────────────────────────────────────────────────

    /**
     * Restituisce la medaglia corrispondente alla posizione data.
     *
     * @param posizione posizione in classifica (1-based)
     * @return la medaglia appropriata, mai null
     */
    public static Medaglia fromPosizione(int posizione) {
        for (Medaglia m : values())
            if (posizione <= m.soglia) return m;
        return NESSUNA;
    }

    @Override
    public String toString() {
        return simbolo.isEmpty() ? name() : simbolo + " " + name();
    }
}
