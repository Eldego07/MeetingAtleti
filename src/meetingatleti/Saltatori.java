package meetingatleti;

/**
 * Atleta saltatore (lungo, alto, triplo, asta).
 * Implementa ISaltatore.
 *
 * calcolaPunteggio() delega alla Prestazione della gara corrente,
 * supportando il multi-disciplina con lo stesso numero di maglia.
 *
 * Formula (gestita da Prestazione): punteggio = distanzaSalto in cm.
 */
public class Saltatori extends Atleta implements ISaltatore {

    public Saltatori() {}

    public Saltatori(String nome, String sesso, Integer eta, Integer pettorale) {
        super(nome, sesso, eta, pettorale);
    }

    @Override
    public int calcolaPunteggio() {
        Gara garaCorrente = AppData.getInstance().getGaraCorrente();
        return calcolaPunteggio(garaCorrente);
    }

    @Override
    public Integer getDistanzaSalto() {
        Gara g = AppData.getInstance().getGaraCorrente();
        if (g == null) return null;
        Prestazione p = getPrestazione(g);
        return (p != null) ? p.getDistanzaSalto() : null;
    }

    @Override
    public void setDistanzaSalto(Integer distanza) {
        Gara g = AppData.getInstance().getGaraCorrente();
        if (g == null) return;
        Prestazione p = getPrestazione(g);
        if (p != null && p.getTipo() == Prestazione.Tipo.SALTO)
            p.setDistanzaSalto(distanza);
    }

    @Override
    public String toString() {
        Gara g = AppData.getInstance().getGaraCorrente();
        Prestazione p = (g != null) ? getPrestazione(g) : null;
        String stat = (p == null) ? "–" : p.getStatisticaLabel();
        return "[" + pettorale + "] " + nome + " | Saltatore"
               + " | " + stat + " | Punteggio: " + calcolaPunteggio();
    }
}
