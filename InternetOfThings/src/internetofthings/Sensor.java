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
public class Sensor extends Thread
{
    //attributi funzionali
    private Cloud myCloud;
    private Environment myEnvironment;
    private Random rnd;
    private double errore;
    private double temperaturaLettaVera;
    private double luminositaLettaVera;
    private double temperaturaLettaConErrore;
    private double luminositaLettaConErrore;
    private BufferedWriter buffWriterT;
    private BufferedWriter buffWriterL;
    
    public Sensor(String name, Cloud c, Environment e, BufferedWriter bT, BufferedWriter bL)
    {
        super(name);
        this.myCloud = c;
        this.myEnvironment = e;
        this.rnd = new Random();
        this.errore = (this.rnd.nextInt(21)-10);
        this.buffWriterT = bT;
        this.buffWriterL = bL;
    }
    
    @Override
    public void run()
    {
        boolean isAlive = true;
        
        // terminazione deferita
        while(isAlive && !this.isInterrupted()){
            
            double valorePercentualeTemp; 
            double valorePercentualeLum;
            try
            {
                this.myEnvironment.measureParameters(this);
				
                //calcolo l'errore del sensore
                valorePercentualeTemp = this.temperaturaLettaVera*this.errore/100;
                this.temperaturaLettaConErrore = this.temperaturaLettaVera + valorePercentualeTemp;
                valorePercentualeLum = this.luminositaLettaVera*this.errore/100;
                this.luminositaLettaConErrore = this.luminositaLettaVera + valorePercentualeLum;
				
		//seleziono i valori registrati da un sensore per punto 3 sezione Testing della relazione
                if(this.getName().equals("Sensor_3"))
                {
                    System.out.println(this.getName() + " legge Temperatura: "+this.temperaturaLettaConErrore+ " Luminosità: "+this.luminositaLettaConErrore);
                    try
                    {
                        this.buffWriterT.write(String.valueOf(this.temperaturaLettaConErrore+"\n"));
                        this.buffWriterT.flush();
                        this.buffWriterL.write(String.valueOf(this.luminositaLettaConErrore+"\n"));
                        this.buffWriterL.flush();
                    }
                    catch(IOException e)
                    {
                        System.out.println(e);
                    }
                }
                this.myCloud.writeData(this);
                Thread.sleep(400);
            }
            catch(InterruptedException e)
            {
                isAlive = false;
                System.out.println(e);
            }
        }
        System.out.println(super.getName()+" termina l'esecuzione!");
    }
    
    public void setTemperaturaLetta(double temperatura)
    {
        this.temperaturaLettaVera = temperatura;
    }
    
    public void setLuminositaLetta(double luminosita)
    {
        this.luminositaLettaVera = luminosita;
    }
    
    public double getTemperaturaLettaConErrore()
    {
        return this.temperaturaLettaConErrore;
    }
    
    public double getLuminositaLettaConErrore()
    {
        return this.luminositaLettaConErrore;
    } 
}
