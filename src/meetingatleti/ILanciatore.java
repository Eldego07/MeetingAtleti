package meetingatleti;

/**
 * Interfaccia di specializzazione per gli atleti lanciatori / pesisti.
 * Implementata da Lanciatori.
 */
public interface ILanciatore {

    Integer getDistanzaLancio();

    /** @param distanza distanza del lancio in cm */
    void setDistanzaLancio(Integer distanza);
}
