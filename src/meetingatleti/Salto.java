package meetingatleti;

import java.io.*;
import java.util.*;

/**
 * Interfaccia per le gare di salto.
 * Implementata da Meeting per filtrare le gare di tipo Salto.
 */
public interface Salto {
    ArrayList<Gara> getGareS();
    ArrayList<Atleta> getAtletiS();
}
