package meetingatleti;

/**
 * Singleton: mantiene il Meeting attivo e la Gara correntemente selezionata.
 * Usato dai form per condividere dati senza dipendenze circolari.
 */
public class AppData {

    private static AppData instance;

    private Meeting meeting;
    private Gara    garaCorrente;

    private AppData() {
        meeting = new Meeting("Meeting di Atletica", "2026-03-15", "Stadio");
    }

    public static AppData getInstance() {
        if (instance == null) instance = new AppData();
        return instance;
    }

    public Meeting getMeeting()            { return meeting; }
    public void    setMeeting(Meeting m)   { this.meeting = m; }

    public Gara    getGaraCorrente()       { return garaCorrente; }
    public void    setGaraCorrente(Gara g) { this.garaCorrente = g; }
}
