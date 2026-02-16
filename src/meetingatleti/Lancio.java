package meetingatleti;

import java.io.*;
import java.util.*;

/**
 * Interfaccia per le gare di lancio.
 * Implementata da Meeting per filtrare le gare di tipo Lancio.
 */
public interface Lancio {
    ArrayList<Gara> getGareL();
    ArrayList<Atleta> getAtletiL();
}
