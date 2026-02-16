package meetingatleti;

/**
 * Interfaccia di specializzazione per gli atleti saltatori.
 * Implementata da Saltatori.
 */
public interface ISaltatore {

    Integer getDistanzaSalto();

    /** @param distanza distanza del salto in cm */
    void setDistanzaSalto(Integer distanza);
}
