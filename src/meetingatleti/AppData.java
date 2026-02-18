package meetingatleti;

import java.util.ArrayList;

/**
 * Singleton: mantiene il Meeting attivo, la Gara corrente, e gli atleti liberi.
 * Usato dai form per condividere dati senza dipendenze circolari.
 */
public class AppData {

    private static AppData instance;

    private Meeting meeting;
    private Gara    garaCorrente;
    private ArrayList<Atleta> atletiLiberi;  // atleti creati senza gara specifica

    private AppData() {
        meeting       = new Meeting("Meeting di Atletica", "2026-03-15", "Stadio");
        atletiLiberi  = new ArrayList<>();
    }

    public static AppData getInstance() {
        if (instance == null) instance = new AppData();
        return instance;
    }

    public Meeting getMeeting()            { return meeting; }
    public void    setMeeting(Meeting m)   { this.meeting = m; }

    public Gara    getGaraCorrente()       { return garaCorrente; }
    public void    setGaraCorrente(Gara g) { this.garaCorrente = g; }

    public ArrayList<Atleta> getAtletiLiberi()  { return atletiLiberi; }

    /** Rimuove un atleta dal pool liberi (usato quando viene aggiunto ad una gara). */
    public boolean rimuoviAtletaLibero(Atleta a) { return atletiLiberi.remove(a); }

}
