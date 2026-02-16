package meetingatleti;

import java.io.*;
import java.util.*;

/**
 * Rappresenta il meeting di atletica.
 * Implementa Corsa, Salto, Lancio per filtrare le gare per tipo.
 */
public class Meeting implements Corsa, Salto, Lancio {

    public Meeting() {
        gare = new ArrayList<>();
    }

    public Meeting(String nome, String data, String luogo) {
        this();
        this.nome  = nome;
        this.data  = data;
        this.luogo = luogo;
    }

    private String          nome;
    private String          data;
    private String          luogo;
    private ArrayList<Gara> gare;

    // ── getter/setter ──────────────────────────────────────────────────────

    public String          getNome()          { return nome; }
    public void            setNome(String n)  { this.nome = n; }

    public String          getData()          { return data; }
    public void            setData(String d)  { this.data = d; }

    public String          getLuogo()         { return luogo; }
    public void            setLuogo(String l) { this.luogo = l; }

    public ArrayList<Gara> getGare()          { return gare; }

    // ── gestione gare ──────────────────────────────────────────────────────

    /** Aggiunge una gara al meeting. */
    public void aggiungiGara(Gara g) {
        if (g != null) gare.add(g);
    }

    /** Rimuove una gara dal meeting. */
    public boolean rimuoviGara(Gara g) { return gare.remove(g); }

    // ── interfacce Corsa / Salto / Lancio ─────────────────────────────────

    @Override
    public ArrayList<Gara> getGareC() {
        ArrayList<Gara> r = new ArrayList<>();
        for (Gara g : gare) if (g.getTipoGaraCorsa() != null)  r.add(g);
        return r;
    }

    @Override
    public ArrayList<Atleta> getAtletiC() {
        ArrayList<Atleta> r = new ArrayList<>();
        for (Gara g : getGareC()) r.addAll(g.getAtleti());
        return r;
    }

    @Override
    public ArrayList<Gara> getGareS() {
        ArrayList<Gara> r = new ArrayList<>();
        for (Gara g : gare) if (g.getTipoGaraSalto() != null)  r.add(g);
        return r;
    }

    @Override
    public ArrayList<Atleta> getAtletiS() {
        ArrayList<Atleta> r = new ArrayList<>();
        for (Gara g : getGareS()) r.addAll(g.getAtleti());
        return r;
    }

    @Override
    public ArrayList<Gara> getGareL() {
        ArrayList<Gara> r = new ArrayList<>();
        for (Gara g : gare) if (g.getTipoGaraLancio() != null) r.add(g);
        return r;
    }

    @Override
    public ArrayList<Atleta> getAtletiL() {
        ArrayList<Atleta> r = new ArrayList<>();
        for (Gara g : getGareL()) r.addAll(g.getAtleti());
        return r;
    }

    // ── riepilogo vincitori ────────────────────────────────────────────────

    /** Lista testuale dei vincitori di ogni gara. */
    public ArrayList<String> getElencoVincitori() {
        ArrayList<String> res = new ArrayList<>();
        for (Gara g : gare) {
            Atleta v = g.trovaVincitore();
            if (v != null)
                res.add(g.getNomeGara() + " → " + v.getNome()
                        + " (pett." + v.getPettorale() + ") – " + v.calcolaPunteggio() + " pt");
            else
                res.add(g.getNomeGara() + " → nessun partecipante");
        }
        return res;
    }

    @Override
    public String toString() {
        return nome + " – " + data + " – " + luogo + " (" + gare.size() + " gare)";
    }
}
