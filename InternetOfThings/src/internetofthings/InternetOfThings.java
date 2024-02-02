/*
 * Progetto Sistemi Operativi Sessione Estiva 2020/2021
 * Autori: Di Fabrizio Giacomo, Montanari Matteo Marco
 */
package internetofthings;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author Giacomo Di Fabrizio, Matteo Marco Montanari
 */
public class InternetOfThings {

    public static void main(String[] args) {
        double tempoZero = System.currentTimeMillis();
        
        Cloud cloud = new Cloud(tempoZero);
        Environment env = new Environment(tempoZero);
        WeatherConditioner wc = new WeatherConditioner("Weather Conditioner", env);
        
        Scanner mioScanner = new Scanner(System.in);
        //ora posso usare lo scanner per ricevere la linea sotto forma di stringa
        
	//acquisisco da riga di comando il numero di sensori
        System.out.println("Inserisci il numero di sensori:");
        int nSensors = -1;
        boolean typeErr;
        do{
            typeErr = false;
            try{
                nSensors = Integer.parseInt(mioScanner.nextLine());
            }catch(NumberFormatException e){
                System.out.println("Formato nSensors non valido! "+e);
                typeErr = true;
            }
            
            if(nSensors <=0 && !typeErr)
            {
                System.out.println("Valore di nSensors non valido!");
            }
            
        }while(nSensors <=0 || typeErr);
        
	//acquisisco da riga di comando il numero di utenti
        System.out.println("Inserisci il numero di utenti:");
        int nUsers = -1;
        do{
            typeErr = false;
            try{
                nUsers = Integer.parseInt(mioScanner.nextLine());
            }catch(NumberFormatException e){
                System.out.println("Formato nUsers non valido! "+e);
                typeErr = true;
            }
            
            if(nUsers <=0 && !typeErr)
            {
                System.out.println("Valore di nUsers non valido!");
            }
            
        }while(nUsers <=0 || typeErr);
        
        //faccio partire il Weather Conditioner
        wc.start();
        
        Sensor[] sensors = new Sensor[nSensors];
        
        try
        {
	    //preparo i file in cui salvare i dati
            FileWriter fwT = new FileWriter("Temperature.txt");
            FileWriter fwL = new FileWriter("Luminosità.txt");
            BufferedWriter bwT = new BufferedWriter(fwT);
            BufferedWriter bwL = new BufferedWriter(fwL);
            
            //faccio partire i sensori
            for(int i=0; i<sensors.length; i++){
                sensors[i] = new Sensor("Sensor_"+i, cloud, env, bwT, bwL);
                sensors[i].start();
            }
        }
        catch(IOException e)
        {
            System.out.println(e);
        }
        
        User[] users = new User[nUsers];
        
        try
        {
	    //preparo il file in cui salvare i dati
            FileWriter fwU = new FileWriter("Tempi letture.txt");
            BufferedWriter bwU = new BufferedWriter(fwU);
            
            //faccio partire gli utenti
            int nRichieste = 100;
            for(int i=0; i<users.length; i++){
                users[i] = new User("User_"+i, cloud, nRichieste, bwU);
                users[i].start();
            }
        }
        catch(IOException e)
        {
            System.out.println(e);
        }
        
        
        //aspetto la terminazione degli utenti
        try{
            for(int i=0; i<users.length; i++){
                users[i].join();
            }
        }catch(InterruptedException e){
            System.out.println(e);
        }
        
        //termino i sensori
        for(int i=0; i<sensors.length; i++){
            sensors[i].interrupt();
        }
        
        //aspetto la terminazione dei sensori
        try{
            for(int i=0; i<sensors.length; i++){
                sensors[i].join();
            }
        }catch(InterruptedException e){
            System.out.println(e);
        }
        
        //termino il Weather Conditioner
        wc.interrupt();
        
        try{
            wc.join();
        }catch(InterruptedException e){
            System.out.println(e);
        }
        
        System.out.println("Simulazione Terminata!");
	//stampo le statistiche
        printStat(users);
    }    
    
    //calcola e stampa la media e la deviazione standard
    public static void printStat(User users[])
    {
        double average = 0;
        double varianza = 0;
        double deviazioneStandard = 0;
        
        for(int i = 0; i < users.length; i++)
        {
            double current = users[i].getWaitingTime();
            average += current;
        }
        average /= users.length;
        System.out.println("AVG: "+average);
        
        for(int i = 0; i < users.length; i++)
        {
            double current = users[i].getWaitingTime();
            varianza += Math.pow(current - average, 2);
        }
        varianza /= users.length;
        deviazioneStandard = Math.pow(varianza, 0.5);
        
        System.out.println("DEVSTD: "+deviazioneStandard);
    }
}
