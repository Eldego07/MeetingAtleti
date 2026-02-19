package meetingatleti;

/**
 * Atleta lanciatore / pesista (peso, disco, martello, giavellotto).
 * Implementa ILanciatore.
 *
 * calcolaPunteggio() delega alla Prestazione della gara corrente,
 * supportando il multi-disciplina con lo stesso numero di maglia.
 *
 * Formula (gestita da Prestazione): punteggio = distanzaLancio in cm.
 */
public class Lanciatori extends Atleta implements ILanciatore {

    public Lanciatori() {}

    public Lanciatori(String nome, String sesso, Integer eta, Integer pettorale) {
        super(nome, sesso, eta, pettorale);
    }

    @Override
    public int calcolaPunteggio() {
        Gara garaCorrente = AppData.getInstance().getGaraCorrente();
        return calcolaPunteggio(garaCorrente);
    }

    @Override
    public Integer getDistanzaLancio() {
        Gara g = AppData.getInstance().getGaraCorrente();
        if (g == null) return null;
        Prestazione p = getPrestazione(g);
        return (p != null) ? p.getDistanzaLancio() : null;
    }

    @Override
    public void setDistanzaLancio(Integer distanza) {
        Gara g = AppData.getInstance().getGaraCorrente();
        if (g == null) return;
        Prestazione p = getPrestazione(g);
        if (p != null && p.getTipo() == Prestazione.Tipo.LANCIO)
            p.setDistanzaLancio(distanza);
    }

    @Override
    public String toString() {
        Gara g = AppData.getInstance().getGaraCorrente();
        Prestazione p = (g != null) ? getPrestazione(g) : null;
        String stat = (p == null) ? "–" : p.getStatisticaLabel();
        return "[" + pettorale + "] " + nome + " | Lanciatore"
               + " | " + stat + " | Punteggio: " + calcolaPunteggio();
    }
}
