package meetingatleti;

import java.io.*;
import java.util.*;

/**
 * Rappresenta una singola gara del meeting.
 * Contiene la lista degli atleti, calcola classifica e vincitore.
 * Implementa Maschile e Femminile per il filtraggio per sesso.
 */
public class Gara implements Maschile, Femminile {

    public Gara() {
        atleti            = new ArrayList<>();
        atletiClassifica  = new ArrayList<>();
    }

    public Gara(String nomeGara, String categoria) {
        this();
        this.nomeGara  = nomeGara;
        this.categoria = categoria;
    }

    // ── campi UML ──────────────────────────────────────────────────────────

    private String            nomeGara;
    private String            categoria;       // "M" o "F"
    private ArrayList<Atleta> atleti;          // iscritti
    private ArrayList<Atleta> atletiClassifica;

    // uno solo di questi viene impostato in base al tipo di gara
    private TipoGaraSalto  tipoGaraSalto;
    private TipoGaraCorsa  tipoGaraCorsa;
    private TipoGaraLancio tipoGaraLancio;

    // ── getter/setter ──────────────────────────────────────────────────────

    public String  getNomeGara()                    { return nomeGara; }
    public void    setNomeGara(String n)            { this.nomeGara = n; }

    public String  getCategoria()                   { return categoria; }
    public void    setCategoria(String c)           { this.categoria = c; }

    public ArrayList<Atleta> getAtleti()            { return atleti; }

    public TipoGaraSalto  getTipoGaraSalto()        { return tipoGaraSalto; }
    public void setTipoGaraSalto(TipoGaraSalto t)   { this.tipoGaraSalto  = t; tipoGaraCorsa = null; tipoGaraLancio = null; }

    public TipoGaraCorsa  getTipoGaraCorsa()        { return tipoGaraCorsa; }
    public void setTipoGaraCorsa(TipoGaraCorsa t)   { this.tipoGaraCorsa  = t; tipoGaraSalto = null; tipoGaraLancio = null; }

    public TipoGaraLancio getTipoGaraLancio()       { return tipoGaraLancio; }
    public void setTipoGaraLancio(TipoGaraLancio t) { this.tipoGaraLancio = t; tipoGaraSalto = null; tipoGaraCorsa = null; }

    public int getNumeroPartecipanti()              { return atleti.size(); }

    /** Ritorna una descrizione del tipo di gara. */
    public String getTipoDescrizione() {
        if (tipoGaraCorsa  != null) return tipoGaraCorsa.name();
        if (tipoGaraSalto  != null) return tipoGaraSalto.name();
        if (tipoGaraLancio != null) return tipoGaraLancio.name();
        return "N/A";
    }

    // ── Maschile / Femminile ───────────────────────────────────────────────

    @Override
    public ArrayList<Atleta> getAtletiM() {
        ArrayList<Atleta> lista = new ArrayList<>();
        for (Atleta a : atleti) if ("M".equalsIgnoreCase(a.getSesso())) lista.add(a);
        return lista;
    }

    @Override
    public ArrayList<Atleta> getAtletiF() {
        ArrayList<Atleta> lista = new ArrayList<>();
        for (Atleta a : atleti) if ("F".equalsIgnoreCase(a.getSesso())) lista.add(a);
        return lista;
    }

    // ── iscrizione ─────────────────────────────────────────────────────────

    /**
     * Iscrive un atleta alla gara.
     * Controlla:
     *   1. Pettorale non duplicato
     *   2. Sesso corrisponde alla categoria della gara
     *   3. Tipo atleta compatibile con il tipo di gara:
     *        Corsa  → solo Velocisti (implements Fondometrista e Ostacolista)
     *        Salto  → solo Saltatori (implements ISaltatore)
     *        Lancio → solo Lanciatori (implements ILanciatore)
     *
     * @return true se iscritto, false se uno dei controlli fallisce
     */
    public boolean iscrizione(Atleta a) {
        if (a == null) return false;

        // 1. pettorale duplicato
        for (Atleta x : atleti)
            if (x.getPettorale().equals(a.getPettorale())) return false;

        // 2. sesso / categoria
        if (!a.getSesso().equalsIgnoreCase(categoria)) return false;

        // 3. tipo atleta compatibile con tipo gara
        if (!tipoCompatibile(a)) return false;

        atleti.add(a);
        return true;
    }

    /**
     * Controlla che il tipo dell'atleta sia compatibile con il tipo della gara.
     *
     *   tipoGaraCorsa  → Velocisti (che implementa Fondometrista e Ostacolista)
     *   tipoGaraSalto  → Saltatori (che implementa ISaltatore)
     *   tipoGaraLancio → Lanciatori (che implementa ILanciatore)
     */
    private boolean tipoCompatibile(Atleta a) {
        if (tipoGaraCorsa  != null) return a instanceof Velocisti;
        if (tipoGaraSalto  != null) return a instanceof Saltatori;
        if (tipoGaraLancio != null) return a instanceof Lanciatori;
        return true; // gara senza tipo: accetta tutti (caso base)
    }

    // ── classifica ─────────────────────────────────────────────────────────

    /**
     * Ordina la lista e la salva in atletiClassifica.
     * Ordine decrescente per punteggio; pari merito → pettorale più basso.
     */
    public void calcolaClassifica() {
        atletiClassifica = new ArrayList<>(atleti);
        atletiClassifica.sort((a1, a2) -> {
            int cmp = Integer.compare(a2.calcolaPunteggio(), a1.calcolaPunteggio());
            return (cmp != 0) ? cmp : Integer.compare(a1.getPettorale(), a2.getPettorale());
        });
    }

    /** Restituisce la classifica (ricalcolando se necessario). */
    public ArrayList<Atleta> getClassifica() {
        if (atletiClassifica.isEmpty() && !atleti.isEmpty()) calcolaClassifica();
        return atletiClassifica;
    }

    // ── vincitore ──────────────────────────────────────────────────────────

    /** @return l'atleta vincitore o null se nessuno iscritto. */
    public Atleta trovaVincitore() {
        if (atleti.isEmpty()) return null;
        calcolaClassifica();
        return atletiClassifica.get(0);
    }

    @Override
    public String toString() {
        return nomeGara + " [" + categoria + "] – " + getTipoDescrizione()
               + " (" + atleti.size() + " atleti)";
    }
}
