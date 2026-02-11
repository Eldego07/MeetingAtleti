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
    protected ArrayList<Atleta> atletiList;

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
            Atleta atleta =new Velocisti();
            atletiList.add(atleta);
        }
        else if(Tipo==Pesista){
            Atleta atleta =new Lanicatori();
            atletiList.add(atleta);
        }
        else{
            Atleta atleta =new Saltatori();
            atletiList.add(atleta);
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