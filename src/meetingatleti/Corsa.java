package meetingatleti;

import java.io.*;
import java.util.*;

/**
 * Interfaccia per le gare di corsa.
 * Implementata da Meeting per filtrare le gare di tipo Corsa.
 */
public interface Corsa {
    ArrayList<Gara> getGareC();
    ArrayList<Atleta> getAtletiC();
}
