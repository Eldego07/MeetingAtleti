package meetingatleti;
import java.io.*;
import java.util.*;

/**
 * 
 */
public class Gara implements Maschile , Femminile{

    /**
     * Default constructor
     */
    public Gara() {
    }

    /**
     * 
     */
    protected ArrayList<Atleta> atleti;

    /**
     * 
     */
    protected ArrayList<Atleta> atletiClassifica;

    /**
     * @return
     */
    public void iscrizione(enum Tipo) {
        // TODO implement here
        if(Tipo==Velocista){
            Atleta atleta =new Velocista();
            atleti
        }
        else if(Tipo==Pesista){
            Atleta atleta =new Pesista();
        }
        else{
            Atleta atleta =new Saltatori();
        }
    }

    @Override
    public ArrayList<Atleta> AtletiM() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public ArrayList<Atleta> AtletiF() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}