/*
 * Progetto Sistemi Operativi Sessione Estiva 2020/2021
 * Autori: Di Fabrizio Giacomo, Montanari Matteo Marco
 */
package internetofthings;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Random;

/**
 *
 * @author Giacomo Di Fabrizio, Matteo Marco Montanari
 */
public class User extends Thread
{
    //attributi funzionali
    private Cloud myCloud;
    private Random rnd;
    private int numRichieste;
    
    private double temperature[];
    private double luminosita[];
    
    private double waitingTime;
    private double sommaWaitingTime;
    
    private BufferedWriter buffWriter;
    
    public User(String name, Cloud c, int n, BufferedWriter bw)
    {
        super(name);
        this.myCloud = c;
        this.rnd = new Random();
        this.numRichieste = n;
        this.temperature = new double[this.numRichieste];
        this.luminosita = new double[this.numRichieste];
        this.buffWriter = bw;
        this.sommaWaitingTime = 0;
    }
    
    @Override
    public void run()
    {
        int timeToSleep;
        for(int i = 0; i < this.numRichieste; i++)
        {
            try
            {
                timeToSleep = this.rnd.nextInt(100);
                Thread.sleep(timeToSleep);
				
		//calcolo il tempo necessario a ottenere i valori di luminosita e temperatura
                double t0 = System.currentTimeMillis();
                this.temperature[i] = this.myCloud.readAverageTemp(this);
                this.luminosita[i] = this.myCloud.readAverageLight(this);
                double t1 = System.currentTimeMillis();
				
                this.waitingTime = (t1 - t0);
                this.sommaWaitingTime += this.waitingTime;
                
		// salvo i tempi di attesa su file
                try
                {
                    this.buffWriter.write(String.valueOf(this.waitingTime+"\n"));
                    this.buffWriter.flush();
                }
                catch(IOException e)
                {
                    System.out.println(e);
                }
            }
            catch(InterruptedException e)
            {
                System.out.println(e);
            }
        }
        System.out.println(super.getName()+" termina l'esecuzione!");
    }
    
    public double getWaitingTime()
    {
        return this.sommaWaitingTime/this.numRichieste;
    }
}