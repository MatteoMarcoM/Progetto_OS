/*
 * Progetto Sistemi Operativi Sessione Estiva 2020/2021
 * Autori: Di Fabrizio Giacomo, Montanari Matteo Marco
 */
package internetofthings;

/**
 *
 * @author Giacomo Di Fabrizio, Matteo Marco Montanari
 */
public class WeatherConditioner extends Thread
{
    //attributi funzionali
    private Environment myEnvironment;
    private double temperaturaDaInviare;
    private double luminositaDaInviare;
    
    public WeatherConditioner(String name, Environment e)
    {
        super(name);
        this.myEnvironment = e;
        this.temperaturaDaInviare = 0;
        this.luminositaDaInviare = 0;
    }
    
    @Override
    public void run()
    {
        boolean isAlive = true;
		
	// terminazione deferita
        while(isAlive && !this.isInterrupted())
        {
            try
            {
                Thread.sleep(400);
                this.luminositaDaInviare = this.luminositaDaInviare + 1000;
                this.temperaturaDaInviare = 10 + 0.00022*this.luminositaDaInviare;
                this.myEnvironment.updateParameters(this, this.temperaturaDaInviare, this.luminositaDaInviare);
            }
            catch(InterruptedException e)
            {
                isAlive = false;
                System.out.println(e);
            }
        }
        System.out.println(super.getName()+" termina l'esecuzione!");
    }
}
