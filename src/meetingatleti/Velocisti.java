package meetingatleti;

import java.io.*;
import java.util.*;

/**
 * Atleta corridore (velocista, fondometrista, ostacolista).
 * Implementa Fondometrista (tempoReazione) e Ostacolista (tempoOstacolo).
 *
 * ── Logica punteggio ──────────────────────────────────────────────────────
 *
 *  VELOCISTA (centom, duecentom, quattrocentom):
 *    Dati: tempoGara (sec, es. 9.85) + tempoReazione (cs, es. 15)
 *    La partenza è a sparo: il tempo di reazione fa parte della prestazione.
 *    Tempo effettivo = tempoGara + tempoReazione / 100.0
 *    Punteggio = (int)(1000 / tempoEffettivo)
 *    → Un tempo reazione più basso migliorea il punteggio.
 *
 *  FONDOMETRISTA (800m, 1500m, …):
 *    Dati: tempoGara (sec, es. 210.5)
 *    Punteggio = (int)(100000 / tempoGara)
 *    → Scala più alta perché i tempi sono molto maggiori.
 *
 *  OSTACOLISTA:
 *    Dati: tempoGara (sec) + tempoOstacolo (cs di penalità per ogni ostacolo abbattuto)
 *    Tempo penalizzato = tempoGara + tempoOstacolo / 100.0
 *    Punteggio = (int)(1000 / tempoPenalizzato)
 *    → Più ostacoli abbattuti → punteggio più basso.
 *
 *  In FRM_Atleti: TXT_StatisticaUnica = tempoGara in secondi.
 */
public class Velocisti extends Atleta implements Fondometrista, Ostacolista {

    public Velocisti() {}

    public Velocisti(String nome, String sesso, Integer eta, Integer pettorale) {
        this.nome      = nome;
        this.sesso     = sesso;
        this.eta       = eta;
        this.pettorale = pettorale;
    }

    /** Tempo di gara in secondi (es. 9.85 per i 100m). */
    private Double  tempoGara;

    /** Tempo di reazione allo sparo in centesimi di secondo (es. 15 cs = 0.15 s).
     *  Usato solo per i velocisti che partono a sparo. */
    private Integer tempoReazione;

    /** Penalità per ostacoli abbattuti, in centesimi di secondo.
     *  Es. 3 ostacoli × 50 cs ciascuno = 150 cs di penalità. */
    private Integer tempoOstacolo;

    // ── getter / setter ───────────────────────────────────────────────────

    public Double  getTempoGara()               { return tempoGara; }
    public void    setTempoGara(Double t)        { this.tempoGara = t; }

    @Override
    public Integer getTempoReazione()            { return tempoReazione; }
    @Override
    public void    setTempoReazione(Integer t)   { this.tempoReazione = t; }

    @Override
    public Integer getTempoOstacolo()            { return tempoOstacolo; }
    @Override
    public void    setTempoOstacolo(Integer t)   { this.tempoOstacolo = t; }

    // ── tipo corridore (determina la formula) ─────────────────────────────

    public boolean isVelocista()      { return tempoReazione  != null && tempoReazione  > 0 && (tempoOstacolo == null || tempoOstacolo == 0); }
    public boolean isOstacolista()    { return tempoOstacolo  != null && tempoOstacolo  > 0; }
    public boolean isFondometrista()  { return !isVelocista() && !isOstacolista(); }

    // ── calcolaPunteggio ──────────────────────────────────────────────────

    /**
     * Punteggio = 10000 / tempoEffettivo
     *
     * Formula unica per tutti i corridori: meno tempo = più punti, sempre.
     * Il tempoEffettivo varia in base al tipo:
     *
     *   VELOCISTA:     tempoEffettivo = tempoGara + tempoReazione/100  (penalità reazione)
     *   OSTACOLISTA:   tempoEffettivo = tempoGara + tempoOstacolo/100  (penalità ostacoli)
     *   FONDOMETRISTA: tempoEffettivo = tempoGara                       (nessuna penalità)
     *
     * Esempi:
     *   Velocista  52s  + 15cs reazione → 10000 / 52.15 ≈ 191 punti
     *   Fondometrista 200s              → 10000 / 200   =  50 punti
     *   → il più veloce vince sempre, indipendentemente dal tipo
     */
    @Override
    public Integer calcolaPunteggio() {
        if (tempoGara == null || tempoGara <= 0) return 0;

        double penalita = 0.0;
        if (isVelocista() && tempoReazione != null)
            penalita = tempoReazione / 100.0;
        else if (isOstacolista() && tempoOstacolo != null)
            penalita = tempoOstacolo / 100.0;

        double tempoEffettivo = tempoGara + penalita;
        return (tempoEffettivo > 0) ? (int)(10000.0 / tempoEffettivo) : 0;
    }

    // ── toString ──────────────────────────────────────────────────────────

    @Override
    public String toString() {
        String tipo   = isOstacolista() ? "Ostacolista" : (isVelocista() ? "Velocista" : "Fondometrista");
        String detail = "";
        if (isOstacolista())
            detail = " | Tempo: " + tempoGara + "s  Penalità: " + tempoOstacolo + "cs";
        else if (isVelocista())
            detail = " | Tempo: " + tempoGara + "s  Reaz: " + tempoReazione + "cs";
        else
            detail = " | Tempo: " + tempoGara + "s";
        return "[" + pettorale + "] " + nome + " | " + tipo + detail
               + " | Punteggio: " + calcolaPunteggio();
    }
}
