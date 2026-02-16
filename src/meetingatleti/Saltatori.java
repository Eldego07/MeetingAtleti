package meetingatleti;

import java.io.*;
import java.util.*;

/**
 * Atleta saltatore (lungo, alto, triplo, asta).
 * statisticaUnica nel form → distanzaSalto (in cm).
 */
public class Saltatori extends Atleta implements ISaltatore {

    public Saltatori() {}

    public Saltatori(String nome, String sesso, Integer eta, Integer pettorale) {
        this.nome      = nome;
        this.sesso     = sesso;
        this.eta       = eta;
        this.pettorale = pettorale;
    }

    private Integer distanzaSalto; // distanza in cm

    @Override
    public Integer getDistanzaSalto()             { return distanzaSalto; }
    @Override
    public void    setDistanzaSalto(Integer d)    { this.distanzaSalto = d; }

    /**
     * Punteggio = distanzaSalto (cm).
     * Distanza maggiore → punteggio più alto.
     */
    @Override
    public Integer calcolaPunteggio() {
        if (distanzaSalto == null) return 0;
        return distanzaSalto;
    }

    @Override
    public String toString() {
        return "[" + pettorale + "] " + nome + " | Saltatore"
               + " | Distanza: " + distanzaSalto + " cm"
               + " | Punteggio: " + calcolaPunteggio();
    }
}
