package meetingatleti;

/**
 * Atleta corridore (velocista, fondometrista, ostacolista).
 * Implementa Fondometrista (tempoReazione) e Ostacolista (tempoOstacolo).
 *
 * calcolaPunteggio() è implementato delegando alla Prestazione associata
 * alla gara corrente (AppData.getGaraCorrente()), in modo da supportare
 * il multi-disciplina: lo stesso atleta può avere una Prestazione.velocista()
 * per i 100m e una Prestazione.salto() per il salto in lungo.
 *
 * Formule (gestite da Prestazione):
 *   VELOCISTA     : 10000 / (tempoGara + tempoReazione/100)
 *   OSTACOLISTA   : 10000 / (tempoGara + tempoOstacolo/100)
 *   FONDOMETRISTA : 10000 / tempoGara
 */
public class Velocisti extends Atleta implements Fondometrista, Ostacolista {

    public Velocisti() {}

    public Velocisti(String nome, String sesso, Integer eta, Integer pettorale) {
        super(nome, sesso, eta, pettorale);
    }

    // ── calcolaPunteggio ──────────────────────────────────────────────────

    /**
     * Delega il calcolo alla Prestazione della gara corrente.
     * Il polimorfismo è garantito: questo metodo è l'implementazione concreta
     * del metodo astratto dichiarato in Atleta.
     */
    @Override
    public int calcolaPunteggio() {
        Gara garaCorrente = AppData.getInstance().getGaraCorrente();
        return calcolaPunteggio(garaCorrente);   // metodo final di Atleta
    }

    // ── Fondometrista ─────────────────────────────────────────────────────

    /**
     * Restituisce il tempo di reazione dalla Prestazione della gara corrente.
     * Restituisce null se la gara corrente non ha una Prestazione di tipo VELOCISTA.
     */
    @Override
    public Integer getTempoReazione() {
        Gara g = AppData.getInstance().getGaraCorrente();
        if (g == null) return null;
        Prestazione p = getPrestazione(g);
        return (p != null) ? p.getTempoReazione() : null;
    }

    @Override
    public void setTempoReazione(Integer tempo) {
        Gara g = AppData.getInstance().getGaraCorrente();
        if (g == null) return;
        Prestazione p = getPrestazione(g);
        if (p != null && p.getTipo() == Prestazione.Tipo.VELOCISTA)
            getPrestazione(g).setTempoReazione(tempo);
    }

    // ── Ostacolista ───────────────────────────────────────────────────────

    @Override
    public Integer getTempoOstacolo() {
        Gara g = AppData.getInstance().getGaraCorrente();
        if (g == null) return null;
        Prestazione p = getPrestazione(g);
        return (p != null) ? p.getTempoOstacolo() : null;
    }

    @Override
    public void setTempoOstacolo(Integer tempo) {
        Gara g = AppData.getInstance().getGaraCorrente();
        if (g == null) return;
        Prestazione p = getPrestazione(g);
        if (p != null && p.getTipo() == Prestazione.Tipo.OSTACOLISTA)
            p.setTempoOstacolo(tempo);
    }

    // ── helper tipo (basati sulla Prestazione della gara corrente) ────────

    public boolean isVelocista() {
        Gara g = AppData.getInstance().getGaraCorrente();
        if (g == null) return false;
        Prestazione p = getPrestazione(g);
        return p != null && p.getTipo() == Prestazione.Tipo.VELOCISTA;
    }

    public boolean isOstacolista() {
        Gara g = AppData.getInstance().getGaraCorrente();
        if (g == null) return false;
        Prestazione p = getPrestazione(g);
        return p != null && p.getTipo() == Prestazione.Tipo.OSTACOLISTA;
    }

    public boolean isFondometrista() {
        Gara g = AppData.getInstance().getGaraCorrente();
        if (g == null) return false;
        Prestazione p = getPrestazione(g);
        return p != null && p.getTipo() == Prestazione.Tipo.FONDOMETRISTA;
    }

    public Double getTempoGara() {
        Gara g = AppData.getInstance().getGaraCorrente();
        if (g == null) return null;
        Prestazione p = getPrestazione(g);
        return (p != null) ? p.getTempoGara() : null;
    }

    @Override
    public String toString() {
        Gara g = AppData.getInstance().getGaraCorrente();
        Prestazione p = (g != null) ? getPrestazione(g) : null;
        String tipo   = (p == null) ? "Corridore" : p.getTipo().name();
        String stat   = (p == null) ? "" : "  |  " + p.getStatisticaLabel();
        return "[" + pettorale + "] " + nome + " | " + tipo + stat
               + " | Punteggio: " + calcolaPunteggio();
    }
}
