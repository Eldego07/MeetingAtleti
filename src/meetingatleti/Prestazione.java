package meetingatleti;

/**
 * Incapsula la prestazione di un atleta in una singola gara.
 *
 * Separando la prestazione dall'atleta, lo stesso oggetto Atleta
 * può partecipare a gare di tipo diverso (corsa, salto, lancio) con
 * lo stesso numero di maglia, senza creare oggetti aggiuntivi.
 *
 * Tipi supportati:
 *   VELOCISTA     – tempo gara + tempo di reazione
 *   OSTACOLISTA   – tempo gara + penalità ostacoli
 *   FONDOMETRISTA – solo tempo gara
 *   SALTO         – distanza salto in cm
 *   LANCIO        – distanza lancio in cm
 *
 * Formule punteggio:
 *   VELOCISTA     : 10000 / (tempoGara + tempoReazione / 100)
 *   OSTACOLISTA   : 10000 / (tempoGara + tempoOstacolo / 100)
 *   FONDOMETRISTA : 10000 / tempoGara
 *   SALTO         : distanzaSalto (cm)
 *   LANCIO        : distanzaLancio (cm)
 */
public class Prestazione {

    // ── enum Tipo ──────────────────────────────────────────────────────────

    public enum Tipo {
        VELOCISTA,
        OSTACOLISTA,
        FONDOMETRISTA,
        SALTO,
        LANCIO;

        public boolean isCorsa() {
            return this == VELOCISTA || this == OSTACOLISTA || this == FONDOMETRISTA;
        }
    }

    // ── campi ──────────────────────────────────────────────────────────────

    private final Tipo tipo;

    private Double  tempoGara;
    private Integer tempoReazione;
    private Integer tempoOstacolo;
    private Integer distanzaSalto;
    private Integer distanzaLancio;

    private Prestazione(Tipo tipo) { this.tipo = tipo; }

    // ── factory method ─────────────────────────────────────────────────────

    public static Prestazione velocista(Double tempoGara, Integer tempoReazione) {
        Prestazione p = new Prestazione(Tipo.VELOCISTA);
        p.tempoGara     = tempoGara;
        p.tempoReazione = tempoReazione;
        return p;
    }

    public static Prestazione ostacolista(Double tempoGara, Integer tempoOstacolo) {
        Prestazione p = new Prestazione(Tipo.OSTACOLISTA);
        p.tempoGara     = tempoGara;
        p.tempoOstacolo = tempoOstacolo;
        return p;
    }

    public static Prestazione fondometrista(Double tempoGara) {
        Prestazione p = new Prestazione(Tipo.FONDOMETRISTA);
        p.tempoGara = tempoGara;
        return p;
    }

    public static Prestazione salto(Integer distanzaSalto) {
        Prestazione p = new Prestazione(Tipo.SALTO);
        p.distanzaSalto = distanzaSalto;
        return p;
    }

    public static Prestazione lancio(Integer distanzaLancio) {
        Prestazione p = new Prestazione(Tipo.LANCIO);
        p.distanzaLancio = distanzaLancio;
        return p;
    }

    // ── calcolo punteggio ─────────────────────────────────────────────────

    public int calcolaPunteggio() {
        switch (tipo) {
            case VELOCISTA:
                if (tempoGara == null || tempoGara <= 0) return 0;
                double penReaz = (tempoReazione != null) ? tempoReazione / 100.0 : 0.0;
                return (int)(10000.0 / (tempoGara + penReaz));

            case OSTACOLISTA:
                if (tempoGara == null || tempoGara <= 0) return 0;
                double penOst = (tempoOstacolo != null) ? tempoOstacolo / 100.0 : 0.0;
                return (int)(10000.0 / (tempoGara + penOst));

            case FONDOMETRISTA:
                if (tempoGara == null || tempoGara <= 0) return 0;
                return (int)(10000.0 / tempoGara);

            case SALTO:
                return (distanzaSalto != null) ? distanzaSalto : 0;

            case LANCIO:
                return (distanzaLancio != null) ? distanzaLancio : 0;

            default: return 0;
        }
    }

    // ── getter ─────────────────────────────────────────────────────────────

    public Tipo    getTipo()            { return tipo; }
    public Double  getTempoGara()       { return tempoGara; }
    public Integer getTempoReazione()   { return tempoReazione; }
    public Integer getTempoOstacolo()   { return tempoOstacolo; }
    public Integer getDistanzaSalto()   { return distanzaSalto; }
    public Integer getDistanzaLancio()  { return distanzaLancio; }

    // ── setter (usati dai getter delle sottoclassi di Atleta) ─────────────

    public void setTempoGara(Double t)       { this.tempoGara     = t; }
    public void setTempoReazione(Integer t)  { this.tempoReazione = t; }
    public void setTempoOstacolo(Integer t)  { this.tempoOstacolo = t; }
    public void setDistanzaSalto(Integer d)  { this.distanzaSalto = d; }
    public void setDistanzaLancio(Integer d) { this.distanzaLancio = d; }

    // ── compatibilità con tipo gara ────────────────────────────────────────

    public boolean compatibileCorsa()  { return tipo.isCorsa(); }
    public boolean compatibileSalto()  { return tipo == Tipo.SALTO; }
    public boolean compatibileLancio() { return tipo == Tipo.LANCIO; }

    // ── etichetta statistica (per le tabelle Swing) ────────────────────────

    public String getStatisticaLabel() {
        switch (tipo) {
            case VELOCISTA:    return tempoGara + "s  reaz:" + tempoReazione + "cs";
            case OSTACOLISTA:  return tempoGara + "s  pen:"  + tempoOstacolo + "cs";
            case FONDOMETRISTA:return tempoGara + "s";
            case SALTO:        return distanzaSalto  + " cm";
            case LANCIO:       return distanzaLancio + " cm";
            default:           return "–";
        }
    }

    @Override
    public String toString() {
        return tipo.name() + " | " + getStatisticaLabel()
               + " | " + calcolaPunteggio() + " pt";
    }
}
