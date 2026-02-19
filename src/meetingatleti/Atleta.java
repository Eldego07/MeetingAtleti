package meetingatleti;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Classe astratta base per tutti gli atleti del meeting.
 *
 * Atleta è astratta e dichiara calcolaPunteggio() come metodo astratto,
 * implementato da ciascuna sottoclasse (Velocisti, Saltatori, Lanciatori).
 *
 * Supporto multi-disciplina (stesso numero di maglia, più gare):
 * La mappa interna "prestazioni" associa a ogni Gara la relativa Prestazione.
 * Lo stesso oggetto Atleta può partecipare a gare di tipo diverso:
 *   gara100m.iscrizione(mario, Prestazione.velocista(9.95, 15));
 *   garaLungo.iscrizione(mario, Prestazione.salto(720));   // stesso oggetto!
 *
 * Le sottoclassi implementano calcolaPunteggio() delegando alla Prestazione
 * della gara corrente (AppData.getGaraCorrente()), garantendo il polimorfismo
 * richiesto dalla traccia.
 */
public abstract class Atleta {

    public Atleta() {
        prestazioni = new LinkedHashMap<>();
    }

    public Atleta(String nome, String sesso, Integer eta, Integer pettorale) {
        this();
        this.nome      = nome;
        this.sesso     = sesso;
        this.eta       = eta;
        this.pettorale = pettorale;
    }

    // ── dati anagrafici ────────────────────────────────────────────────────

    protected String  nome;
    protected String  sesso;      // "M" o "F"
    protected Integer eta;
    protected Integer pettorale;  // numero maglia – identifica l'atleta nel meeting

    // ── mappa prestazioni (una voce per ogni gara a cui partecipa) ─────────

    /**
     * Mappa Gara → Prestazione.
     * Permette allo stesso oggetto Atleta di avere prestazioni diverse
     * in gare diverse, con lo stesso numero di maglia.
     */
    private final Map<Gara, Prestazione> prestazioni;

    // ── getter/setter anagrafici ───────────────────────────────────────────

    public String  getNome()                       { return nome; }
    public void    setNome(String nome)            { this.nome = nome; }

    public String  getSesso()                      { return sesso; }
    public void    setSesso(String sesso)          { this.sesso = sesso; }

    public Integer getEta()                        { return eta; }
    public void    setEta(Integer eta)             { this.eta = eta; }

    public Integer getPettorale()                  { return pettorale; }
    public void    setPettorale(Integer pettorale) { this.pettorale = pettorale; }

    // ── gestione prestazioni ───────────────────────────────────────────────

    /**
     * Registra la prestazione per una gara specifica.
     * Chiamato da Gara.iscrizione(Atleta, Prestazione).
     */
    public void aggiungiPrestazione(Gara gara, Prestazione prestazione) {
        if (gara == null)        throw new IllegalArgumentException("gara non puo essere null");
        if (prestazione == null) throw new IllegalArgumentException("prestazione non puo essere null");
        prestazioni.put(gara, prestazione);
    }

    /**
     * Restituisce la prestazione per la gara indicata, o null se non iscritto.
     */
    public Prestazione getPrestazione(Gara gara) {
        return prestazioni.get(gara);
    }

    /** True se l'atleta ha una prestazione registrata per la gara indicata. */
    public boolean haPartecipato(Gara gara) { return prestazioni.containsKey(gara); }

    /** Gare a cui l'atleta è iscritto (ordine di iscrizione). */
    public Set<Gara> getGarePartecipate()   { return prestazioni.keySet(); }

    /** Numero di gare a cui l'atleta partecipa. */
    public int getNumeroGare()              { return prestazioni.size(); }

    /** Rimuove l'atleta da una gara (es. ritiro). */
    public boolean rimuoviPrestazione(Gara gara) { return prestazioni.remove(gara) != null; }

    // ── metodo astratto – implementato dalle sottoclassi ──────────────────

    /**
     * Calcola il punteggio per la GARA CORRENTE (AppData.getGaraCorrente()).
     *
     * Le sottoclassi implementano questo metodo recuperando la Prestazione
     * associata alla gara corrente dalla mappa interna e delegando a essa
     * il calcolo numerico:
     *
     *   Prestazione p = getPrestazione(AppData.getInstance().getGaraCorrente());
     *   return (p != null) ? p.calcolaPunteggio() : 0;
     *
     * In questo modo il polimorfismo è rispettato (metodo astratto implementato
     * da ogni sottoclasse) e il multi-disciplina è supportato (la Prestazione
     * varia per gara, non per tipo di atleta).
     */
    public abstract int calcolaPunteggio();

    /**
     * Calcola il punteggio per una gara specifica.
     * Metodo di convenienza usato da GestorePunteggio per evitare dipendenze
     * dal contesto AppData.
     *
     * @param gara la gara per cui calcolare il punteggio
     * @return punteggio >= 0
     */
    public final int calcolaPunteggio(Gara gara) {
        Prestazione p = getPrestazione(gara);
        return (p != null) ? p.calcolaPunteggio() : 0;
    }

    /**
     * Totale dei punti su tutte le gare del meeting.
     */
    public int calcolaPunteggioTotale() {
        int totale = 0;
        for (Prestazione p : prestazioni.values()) totale += p.calcolaPunteggio();
        return totale;
    }

    // ── utility ────────────────────────────────────────────────────────────

    /** Riepilogo testuale di tutte le prestazioni dell'atleta. */
    public String riepilogoPrestazioni() {
        if (prestazioni.isEmpty()) return "[" + pettorale + "] " + nome + " - nessuna prestazione";
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(pettorale).append("] ").append(nome).append(":\n");
        for (Map.Entry<Gara, Prestazione> e : prestazioni.entrySet())
            sb.append("  ").append(e.getKey().getNomeGara())
              .append(" -> ").append(e.getValue()).append("\n");
        return sb.toString().trim();
    }

    @Override
    public String toString() {
        return "[" + pettorale + "] " + nome
               + " (" + sesso + ", " + eta + " anni)"
               + " - " + getNumeroGare() + " gare";
    }
}
