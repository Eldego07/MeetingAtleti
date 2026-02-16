package meetingatleti;

import java.io.*;
import java.util.*;

/**
 * Atleta velocista (100m, 200m, 400m, ostacoli).
 * Implementa Fondometrista (tempoReazione) e Ostacolista (tempoOstacolo).
 *
 * statisticaUnica nel form → velocitaCorsa (velocità in km/h o tempo in sec)
 */
public class Velocisti extends Atleta implements Fondometrista, Ostacolista {

    public Velocisti() {}

    public Velocisti(String nome, String sesso, Integer eta, Integer pettorale) {
        this.nome      = nome;
        this.sesso     = sesso;
        this.eta       = eta;
        this.pettorale = pettorale;
    }

    private Integer velocitaCorsa;   // statistica principale
    private Integer tempoReazione;   // da Fondometrista
    private Integer tempoOstacolo;   // da Ostacolista (0 se gara piana)

    // ── getter/setter ──────────────────────────────────────────────────────

    public Integer getVelocitaCorsa()              { return velocitaCorsa; }
    public void    setVelocitaCorsa(Integer v)     { this.velocitaCorsa = v; }

    @Override public Integer getTempoReazione()            { return tempoReazione; }
    @Override public void    setTempoReazione(Integer t)   { this.tempoReazione = t; }

    @Override public Integer getTempoOstacolo()            { return tempoOstacolo; }
    @Override public void    setTempoOstacolo(Integer t)   { this.tempoOstacolo = t; }

    /**
     * Punteggio = velocitaCorsa (km/h).
     * Velocità maggiore → punteggio più alto.
     * Il tempoReazione negativo abbassa il punteggio se rilevante.
     */
    @Override
    public Integer calcolaPunteggio() {
        if (velocitaCorsa == null) return 0;
        return velocitaCorsa;
    }

    @Override
    public String toString() {
        return "[" + pettorale + "] " + nome + " | Velocista"
               + " | Vel: " + velocitaCorsa + " km/h"
               + " | Punteggio: " + calcolaPunteggio();
    }
}
