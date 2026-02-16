package meetingatleti;

import java.io.*;
import java.util.*;

/**
 * Interfaccia per atleti che corrono su ostacoli.
 * Il tempoOstacolo rappresenta la penalità accumulata abbattendo ostacoli.
 */
public interface Ostacolista {

    Integer getTempoOstacolo();

    /** @param tempo penalità in centesimi di secondo */
    void setTempoOstacolo(Integer tempo);
}
