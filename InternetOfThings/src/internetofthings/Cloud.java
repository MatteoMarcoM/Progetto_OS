/*
 * Progetto Sistemi Operativi Sessione Estiva 2020/2021
 * Autori: Di Fabrizio Giacomo, Montanari Matteo Marco
 */
package internetofthings;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Giacomo Di Fabrizio, Matteo Marco Montanari
 */

//oggetto condiviso tra sensori e utenti
public class Cloud 
{
    //attributi funzionali
    private static final int CAPIENZABUFFER = 30;
    private int capienzaBuffer;
    private double bufferTemperature[];
    private double bufferLuminosita[];
    private int inBufferTemp;
    private int outBufferTemp;
    private int inBufferLum;
    private int outBufferLum;
    private int elementiBufferTemp;
    private int elementiBufferLum;
    private double startTime;
    
    //attributi di sincronizzazione
    private ReentrantLock mutexTemperatura;
    private ReentrantLock mutexLuminosita;
    
    private Condition notFullTemp;   //produttore
    private Condition notFullLum;    //produttore
    private Condition notEmptyTemp;  //consumatore
    private Condition notEmptyLum;   //consumatore
    
    public Cloud(double tempo)
    {
        this.capienzaBuffer = Cloud.CAPIENZABUFFER;
        this.bufferTemperature = new double[this.capienzaBuffer];
        this.bufferLuminosita = new double[this.capienzaBuffer];
        this.inBufferTemp = 0;
        this.outBufferTemp = 0;
        this.inBufferLum = 0;
        this.outBufferTemp = 0;
        this.elementiBufferTemp = 0;
        this.elementiBufferLum = 0;
        this.startTime = tempo;
        
        this.mutexTemperatura = new ReentrantLock();
        this.mutexLuminosita = new ReentrantLock();
        this.notFullTemp = this.mutexTemperatura.newCondition();
        this.notFullLum = this.mutexLuminosita.newCondition();
        this.notEmptyTemp = this.mutexTemperatura.newCondition();
        this.notEmptyLum = this.mutexLuminosita.newCondition();
    }
    
    //interfaccia pubblica 
    //consumatore temperatura
    public double readAverageTemp(User usr)
    {
        double element = -1;
        // INIZIO SEZIONE CRITICA
        this.mutexTemperatura.lock();
        try
        {
            while(this.elementiBufferTemp <4) 
            {
                this.notEmptyTemp.await();
            }
            // se eseguo qua significa che almeno 4 elementi da consumare sono presenti nel buffer
            
            double somma = 0;
            for(int i=0; i<4; i++){
                somma += this.bufferTemperature[(this.outBufferTemp+i) %this.bufferTemperature.length];
            }
            element = somma/4;
            System.out.println(this.getTime()+" --> "+usr.getName()+" legge il valore medio della temperatura: "+element);
            
            this.outBufferTemp = (this.outBufferTemp + 4) % this.bufferTemperature.length;
            this.elementiBufferTemp -= 4;
            
            // segnalo al Produttore che ora ci sono altri 4 posti disponibili nel buffer.
            for(int i = 0; i < 4 ; i++)
            {
                this.notFullTemp.signal();
            }
        }
        catch (InterruptedException e)
        {
            System.out.println(e);
        }
        finally
        {
            this.mutexTemperatura.unlock();
            //FINE SEZIONE CRITICA
        }
        return element;
    }
    
    //consumatore luminosita'
    public double readAverageLight(User usr)
    {
        double element = -1;
        // INIZIO SEZIONE CRITICA
        this.mutexLuminosita.lock();
        try
        {
            while(this.elementiBufferLum < 4) 
            {
                this.notEmptyLum.await();
            }
            // se eseguo qua significa che almeno un elemento da consumare
            // è presente nel buffer
      
            double somma = 0;
            for(int i=0; i<4; i++){
                somma += this.bufferLuminosita[(this.outBufferLum+i) % this.bufferLuminosita.length];
            }
            element = somma/4;
            System.out.println(this.getTime()+" --> "+usr.getName()+" legge il valore medio della luminosita: "+element);
            
            this.outBufferLum = (this.outBufferLum + 4) % this.bufferLuminosita.length;
            this.elementiBufferLum -= 4;
            // segnalo al Produttore che ora ci sono altri 4 posti 
            // disponibili nel buffer.
            for(int i = 0; i < 4; i++)
            {
                this.notFullLum.signal();   
            }
        }
        catch (InterruptedException e)
        {
            System.out.println(e);
        }
        finally
        {
            this.mutexLuminosita.unlock();
            //FINE SEZIONE CRITICA
        }
        return element;
    }
    
    //produttore
    public void writeData(Sensor s) throws InterruptedException
    {
        double temp = s.getTemperaturaLettaConErrore();
        double lum = s.getLuminositaLettaConErrore();
        
	//INIZIO SEZIONE CRITICA TEMPERATURA
        this.mutexTemperatura.lock();
        try{
            while(this.elementiBufferTemp == this.bufferTemperature.length)
            {
                this.notFullTemp.await();
            }
            // se eseguo qua significa che è presente almeno un posto libero per scrivere nel buffer
            this.elementiBufferTemp++;
            this.bufferTemperature[this.inBufferTemp] = temp;
            this.inBufferTemp = (this.inBufferTemp + 1) % this.bufferTemperature.length;
            System.out.println(this.getTime()+" --> "+s.getName()+" scrive sul buffer temperature: "+temp);
            
	    // segnalo al consumatore che c'è un nuovo elemento nel buffer
            // se il consumatore stava dormendo si risveglierà
            this.notEmptyTemp.signal();

        }finally{
            this.mutexTemperatura.unlock();
	    //FINE SEZIONE CRITICA TEMPERATURA
        }
        
        //INIZIO SEZIONE CRITICA LUMINOSTIA'
        this.mutexLuminosita.lock();
        try{
            while(this.elementiBufferLum == this.bufferLuminosita.length)
            {
                this.notFullLum.await();
            }
            // se eseguo qua significa che è presente almeno un posto libero per scrivere nel buffer
            this.elementiBufferLum++;
            this.bufferLuminosita[this.inBufferLum] = lum;
            this.inBufferLum = (this.inBufferLum + 1) % this.bufferLuminosita.length;
            System.out.println(this.getTime()+" --> "+s.getName()+" scrive sul buffer luminosita: "+lum);
            // segnalo al consumatore che c'è un nuovo elemento nel buffer
            // se il consumatore stava dormendo si risveglierà
            this.notEmptyLum.signal();

        }finally{
            this.mutexLuminosita.unlock();
	    //FINE SEZIONE CRITICA LUMINOSITA'
        }
    }
    
    private double getTime()
    {
        return System.currentTimeMillis() - this.startTime;
    }

}
