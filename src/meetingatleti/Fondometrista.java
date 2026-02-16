package meetingatleti;

import java.io.*;
import java.util.*;

/**
 * Interfaccia per atleti che hanno un tempo di reazione rilevante
 * (es. velocisti, fondometristi).
 */
public interface Fondometrista {

    Integer getTempoReazione();

    /** @param tempo tempo di reazione in centesimi di secondo */
    void setTempoReazione(Integer tempo);
}
