package meetingatleti;

import java.io.*;
import java.util.*;

/**
 * Classe astratta base per tutti gli atleti.
 */
public abstract class Atleta {

    public Atleta() {}

    protected String  nome;
    protected String  sesso;      // "M" o "F"
    protected Integer eta;
    protected Integer pettorale;  // numero maglia (nMaglia nel form)

    // ── getter/setter ──────────────────────────────────────────────────────

    public String  getNome()                { return nome; }
    public void    setNome(String nome)     { this.nome = nome; }

    public String  getSesso()               { return sesso; }
    public void    setSesso(String sesso)   { this.sesso = sesso; }

    public Integer getEta()                 { return eta; }
    public void    setEta(Integer eta)      { this.eta = eta; }

    public Integer getPettorale()                    { return pettorale; }
    public void    setPettorale(Integer pettorale)   { this.pettorale = pettorale; }

    // ── metodo astratto ────────────────────────────────────────────────────

    /** Ogni sottoclasse calcola il punteggio in base alla propria statistica. */
    public abstract Integer calcolaPunteggio();

    @Override
    public String toString() {
        return "[" + pettorale + "] " + nome + " (" + sesso + ", " + eta + " anni)"
               + " – punteggio: " + calcolaPunteggio();
    }
}
