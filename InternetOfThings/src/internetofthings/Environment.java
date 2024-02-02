/*
 * Progetto Sistemi Operativi Sessione Estiva 2020/2021
 * Autori: Di Fabrizio Giacomo, Montanari Matteo Marco
 */
package internetofthings;

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Giacomo Di Fabrizio, Matteo Marco Montanari
 */

//oggetto condiviso tra sensori e weather conditioner
public class Environment 
{
    //attributi funzionali
    private double temperaturaCorrente;
    private double luminositaCorrente;
    private double startTime;
    
    //attributi di sincronizzazione
    private int numLettori;
    private ReentrantLock mutexNumLettori;
    private Semaphore mutexScrittore;
    
    public Environment(double tempo)
    {
        this.temperaturaCorrente = 0;
        this.luminositaCorrente = 0;
        this.startTime = tempo;
        
        this.mutexNumLettori = new ReentrantLock();
        this.mutexScrittore = new Semaphore(1);
        this.numLettori = 0;
    }
    
    //interfaccia pubblica
    //possibile starvation scrittore
    //metodo lettori
    public void measureParameters(Sensor s) throws InterruptedException
    {
	//INIZIO SEZIONE CRITICA
        this.mutexNumLettori.lock();
        try{
            this.numLettori++;
            
            // se sono il primo lettore aspetto che lo scrittore finisca e poi lo blocco per entrare in critica
            if(this.numLettori == 1){
                this.mutexScrittore.acquire();
            }    
        }finally{
            this.mutexNumLettori.unlock();
	    //FINE SEZIONE CRITICA
        }
        
        //tutti lettori liberi
        s.setLuminositaLetta(this.luminositaCorrente);
        s.setTemperaturaLetta(this.temperaturaCorrente);
        System.out.println(this.getTime()+" --> "+s.getName()+" legge Temperatura: "+this.temperaturaCorrente+" Luminosità: "+this.luminositaCorrente);
        
        //INIZIO SEZIONE CRITICA
	this.mutexNumLettori.lock();
        try{
            this.numLettori--;
            
            //se sono l'ultimo lettore posso sbloccare lo scrittore
            if(this.numLettori == 0){
                this.mutexScrittore.release();
            }
            
        }finally{
            this.mutexNumLettori.unlock();
	    //FINE SEZIONE CRITICA
        }

    }
    
    //paradigma lettori scrittori sulla temperatura e luminosita'
    //scrittore
    public void updateParameters(WeatherConditioner w, double temperaturaCorrente, double luminositaCorrente) throws InterruptedException
    {
	//INIZIO SEZIONE CRITICA
        this.mutexScrittore.acquire();
        try
        {  
            System.out.println(this.getTime()+" --> "+w.getName()+" aggiorna i valori di temperatura: "+temperaturaCorrente+" e luminosita: "+luminositaCorrente);
            this.temperaturaCorrente = temperaturaCorrente;
            this.luminositaCorrente = luminositaCorrente;
        }
        finally
        {
            this.mutexScrittore.release();
	    //FINE SEZIONE CRITICA
        }
    }
    
    private double getTime()
    {
        return System.currentTimeMillis() - this.startTime;
    }

}