package meetingatleti;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Singleton: mantiene il Meeting attivo, la Gara corrente e gli atleti liberi.
 * Usato dai form per condividere dati senza dipendenze circolari.
 *
 * v3 – aggiunta prestazioniInAttesa:
 * Quando un atleta viene salvato come LIBERO (senza gara attiva), la sua
 * Prestazione viene conservata qui. Quando FRM_Gara lo aggiunge a una gara,
 * recupera la Prestazione da questa mappa e chiama gara.iscrizione(atleta, prestazione).
 */
public class AppData {

    private static AppData instance;

    private Meeting meeting;
    private Gara    garaCorrente;
    private ArrayList<Atleta> atletiLiberi;

    /** Mappa atleta libero → prestazione pre-compilata nel form. */
    private Map<Atleta, Prestazione> prestazioniInAttesa;

    private AppData() {
        meeting              = new Meeting("Meeting di Atletica", "2026-03-15", "Stadio");
        atletiLiberi         = new ArrayList<>();
        prestazioniInAttesa  = new LinkedHashMap<>();
    }

    public static AppData getInstance() {
        if (instance == null) instance = new AppData();
        return instance;
    }

    // ── meeting / gara corrente ────────────────────────────────────────────

    public Meeting getMeeting()            { return meeting; }
    public void    setMeeting(Meeting m)   { this.meeting = m; }

    public Gara    getGaraCorrente()       { return garaCorrente; }
    public void    setGaraCorrente(Gara g) { this.garaCorrente = g; }

    // ── atleti liberi ──────────────────────────────────────────────────────

    public ArrayList<Atleta> getAtletiLiberi() { return atletiLiberi; }

    /**
     * Aggiunge un atleta libero conservando anche la sua prestazione in attesa.
     *
     * @param atleta      l'atleta
     * @param prestazione la prestazione compilata nel form (sarà usata quando
     *                    l'atleta viene iscritto a una gara da FRM_Gara)
     */
    public void aggiungiAtletaLibero(Atleta atleta, Prestazione prestazione) {
        atletiLiberi.add(atleta);
        if (prestazione != null) prestazioniInAttesa.put(atleta, prestazione);
    }

    /**
     * Rimuove un atleta dal pool liberi e cancella la sua prestazione in attesa.
     *
     * @return true se l'atleta era presente
     */
    public boolean rimuoviAtletaLibero(Atleta a) {
        prestazioniInAttesa.remove(a);
        return atletiLiberi.remove(a);
    }

    // ── prestazioni in attesa ──────────────────────────────────────────────

    /**
     * Restituisce la Prestazione pre-compilata di un atleta libero,
     * o null se non disponibile.
     */
    public Prestazione getPrestazioneInAttesa(Atleta a) {
        return prestazioniInAttesa.get(a);
    }

    /**
     * Rimuove la prestazione in attesa senza rimuovere l'atleta dai liberi.
     * Usato dopo che la prestazione è stata consumata dall'iscrizione.
     */
    public void rimuoviPrestazioneInAttesa(Atleta a) {
        prestazioniInAttesa.remove(a);
    }
}
