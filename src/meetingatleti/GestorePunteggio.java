package meetingatleti;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Classe di servizio per la gestione centralizzata dei punteggi.
 *
 * Tutti i metodi sono statici: non è necessario istanziare la classe.
 *
 * v3 – supporto multi-disciplina:
 * I metodi usano direttamente Atleta.getPrestazione(Gara) per ottenere
 * il punteggio corretto senza dipendere dal contesto AppData.garaCorrente.
 *
 * Regole di ranking:
 *   1. Ordine decrescente per punteggio.
 *   2. A pari punteggio: pettorale più basso prima (stessa posizione numerica,
 *      flag pariMerito=true).
 *   3. Medaglie seguono la posizione: due atleti in pos. 2 → entrambi ARGENTO.
 */
public class GestorePunteggio {

    private GestorePunteggio() {}

    // ═══════════════════════════════════════════════════════════════════
    //  CLASSIFICA DI GARA
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Calcola la classifica completa di una gara.
     * Usa Atleta.getPrestazione(gara) per il punteggio: nessuna dipendenza
     * da AppData.garaCorrente.
     *
     * @param gara la gara (non null)
     * @return lista di VocePunteggio ordinata per punteggio decrescente
     */
    public static ArrayList<VocePunteggio> calcolaClassifica(Gara gara) {
        if (gara == null) throw new IllegalArgumentException("gara non puo essere null");

        ArrayList<Atleta> atleti = new ArrayList<>(gara.getAtleti());
        ordinaAtleti(atleti, gara);

        ArrayList<VocePunteggio> classifica = new ArrayList<>();
        int posizioneCorrente = 0;
        int ultimoPunteggio   = Integer.MIN_VALUE;

        for (int i = 0; i < atleti.size(); i++) {
            Atleta a     = atleti.get(i);
            int    punti = punteggioAtleta(a, gara);
            boolean par  = (punti == ultimoPunteggio);
            if (!par) posizioneCorrente = i + 1;
            classifica.add(new VocePunteggio(a, posizioneCorrente, punti, par));
            ultimoPunteggio = punti;
        }
        return classifica;
    }

    /**
     * Restituisce la VocePunteggio del vincitore, o null se nessun atleta iscritto.
     */
    public static VocePunteggio trovaVincitore(Gara gara) {
        if (gara == null || gara.getAtleti().isEmpty()) return null;
        ArrayList<VocePunteggio> cl = calcolaClassifica(gara);
        return cl.isEmpty() ? null : cl.get(0);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  STATISTICHE DI GARA
    // ═══════════════════════════════════════════════════════════════════

    public static int punteggioMassimo(Gara gara) {
        if (gara == null || gara.getAtleti().isEmpty()) return 0;
        int max = Integer.MIN_VALUE;
        for (Atleta a : gara.getAtleti()) { int p = punteggioAtleta(a, gara); if (p > max) max = p; }
        return max;
    }

    public static int punteggioMinimo(Gara gara) {
        if (gara == null || gara.getAtleti().isEmpty()) return 0;
        int min = Integer.MAX_VALUE;
        for (Atleta a : gara.getAtleti()) { int p = punteggioAtleta(a, gara); if (p < min) min = p; }
        return min;
    }

    public static double punteggioMedio(Gara gara) {
        if (gara == null || gara.getAtleti().isEmpty()) return 0.0;
        int somma = 0;
        for (Atleta a : gara.getAtleti()) somma += punteggioAtleta(a, gara);
        return (double) somma / gara.getAtleti().size();
    }

    public static ArrayList<Atleta> trovaPariMeritoPrimo(Gara gara) {
        ArrayList<Atleta> res = new ArrayList<>();
        if (gara == null || gara.getAtleti().isEmpty()) return res;
        int max = punteggioMassimo(gara);
        for (Atleta a : gara.getAtleti()) if (punteggioAtleta(a, gara) == max) res.add(a);
        return (res.size() >= 2) ? res : new ArrayList<>();
    }

    public static String riepilogoStatistiche(Gara gara) {
        if (gara == null || gara.getAtleti().isEmpty()) return "Nessun dato disponibile.";
        ArrayList<Atleta> pari = trovaPariMeritoPrimo(gara);
        String pariStr = pari.isEmpty() ? "-" :
                pari.stream().map(Atleta::getNome).reduce((a, b) -> a + ", " + b).orElse("-")
                + " (" + punteggioAtleta(pari.get(0), gara) + " pt)";
        return String.format(
            "Partecipanti : %d%n" +
            "Punteggio MAX: %d%n" +
            "Punteggio MIN: %d%n" +
            "Media        : %.1f%n" +
            "Pari merito  : %s",
            gara.getAtleti().size(),
            punteggioMassimo(gara),
            punteggioMinimo(gara),
            punteggioMedio(gara),
            pariStr
        );
    }

    // ═══════════════════════════════════════════════════════════════════
    //  CLASSIFICA GENERALE DEL MEETING
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Calcola la classifica generale del meeting, sommando i punti di ogni
     * atleta (nome + pettorale) su tutte le gare a cui ha partecipato.
     */
    public static ArrayList<VocePunteggioMeeting> calcolaClassificaMeeting(Meeting meeting) {
        if (meeting == null) throw new IllegalArgumentException("meeting non puo essere null");

        Map<String, VocePunteggioMeeting> mappa = new LinkedHashMap<>();

        for (Gara gara : meeting.getGare()) {
            for (Atleta a : gara.getAtleti()) {
                String chiave = a.getNome() + "::" + a.getPettorale();
                if (!mappa.containsKey(chiave))
                    mappa.put(chiave, new VocePunteggioMeeting(a.getNome(), a.getPettorale(), a.getSesso()));
                mappa.get(chiave).aggiungiGara(gara.getNomeGara(), punteggioAtleta(a, gara));
            }
        }

        ArrayList<VocePunteggioMeeting> lista = new ArrayList<>(mappa.values());
        lista.sort((v1, v2) -> {
            int cmp = Integer.compare(v2.getTotalePunti(), v1.getTotalePunti());
            return (cmp != 0) ? cmp : Integer.compare(v1.getPettorale(), v2.getPettorale());
        });

        int posCorrente = 0, ultimiPunti = Integer.MIN_VALUE;
        for (int i = 0; i < lista.size(); i++) {
            VocePunteggioMeeting v = lista.get(i);
            boolean par = (v.getTotalePunti() == ultimiPunti);
            if (!par) posCorrente = i + 1;
            v.setPosizione(posCorrente);
            v.setPariMerito(par);
            ultimiPunti = v.getTotalePunti();
        }
        return lista;
    }

    public static ArrayList<VocePunteggioMeeting> filtraPerSesso(
            ArrayList<VocePunteggioMeeting> classifica, String sesso) {
        ArrayList<VocePunteggioMeeting> filtrata = new ArrayList<>();
        for (VocePunteggioMeeting v : classifica)
            if (v.getSesso().equalsIgnoreCase(sesso)) filtrata.add(v);
        ricalcolaPosizioni(filtrata);
        return filtrata;
    }

    public static VocePunteggioMeeting miglioreAtletaMeeting(Meeting meeting) {
        ArrayList<VocePunteggioMeeting> cl = calcolaClassificaMeeting(meeting);
        return cl.isEmpty() ? null : cl.get(0);
    }

    public static String riepilogoClassificaMeeting(Meeting meeting) {
        ArrayList<VocePunteggioMeeting> cl = calcolaClassificaMeeting(meeting);
        if (cl.isEmpty()) return "Nessun atleta registrato nel meeting.";
        StringBuilder sb = new StringBuilder();
        sb.append("=== Classifica Generale - ").append(meeting.getNome()).append(" ===\n");
        for (VocePunteggioMeeting v : cl) sb.append(v.toString()).append("\n");
        return sb.toString();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  UTILITY PACKAGE-PRIVATE
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Punteggio di un atleta per una gara specifica.
     * Usa direttamente Prestazione (nessuna dipendenza da AppData).
     */
    static int punteggioAtleta(Atleta atleta, Gara gara) {
        Prestazione p = atleta.getPrestazione(gara);
        return (p != null) ? p.calcolaPunteggio() : 0;
    }

    private static void ordinaAtleti(ArrayList<Atleta> atleti, Gara gara) {
        atleti.sort((a1, a2) -> {
            int cmp = Integer.compare(punteggioAtleta(a2, gara), punteggioAtleta(a1, gara));
            return (cmp != 0) ? cmp : Integer.compare(a1.getPettorale(), a2.getPettorale());
        });
    }

    private static void ricalcolaPosizioni(ArrayList<VocePunteggioMeeting> lista) {
        int posCorrente = 0, ultimiPunti = Integer.MIN_VALUE;
        for (int i = 0; i < lista.size(); i++) {
            VocePunteggioMeeting v = lista.get(i);
            boolean par = (v.getTotalePunti() == ultimiPunti);
            if (!par) posCorrente = i + 1;
            v.setPosizione(posCorrente);
            v.setPariMerito(par);
            ultimiPunti = v.getTotalePunti();
        }
    }
}
