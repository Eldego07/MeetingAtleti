package meetingatleti;

import java.io.*;
import java.util.*;

/**
 * Atleta lanciatore / pesista (peso, disco, martello, giavellotto).
 * Nel form è elencato come "Pesista".
 * statisticaUnica nel form → distanzaLancio (in cm).
 */
public class Lanciatori extends Atleta implements ILanciatore {

    public Lanciatori() {}

    public Lanciatori(String nome, String sesso, Integer eta, Integer pettorale) {
        this.nome      = nome;
        this.sesso     = sesso;
        this.eta       = eta;
        this.pettorale = pettorale;
    }

    private Integer distanzaLancio; // distanza in cm

    @Override
    public Integer getDistanzaLancio()            { return distanzaLancio; }
    @Override
    public void    setDistanzaLancio(Integer d)   { this.distanzaLancio = d; }

    /**
     * Punteggio = distanzaLancio (cm).
     * Distanza maggiore → punteggio più alto.
     */
    @Override
    public Integer calcolaPunteggio() {
        if (distanzaLancio == null) return 0;
        return distanzaLancio;
    }

    @Override
    public String toString() {
        return "[" + pettorale + "] " + nome + " | Pesista"
               + " | Lancio: " + distanzaLancio + " cm"
               + " | Punteggio: " + calcolaPunteggio();
    }
}
