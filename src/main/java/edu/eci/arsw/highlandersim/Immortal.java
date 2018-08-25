package edu.eci.arsw.highlandersim;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback = null;

    private int health;

    private int defaultDamageValue;

    private final List<Immortal> immortalsPopulation;

    private final String name;

    private final Random r = new Random(System.currentTimeMillis());

    private boolean pausar;

    private final Object lock; //Otra posible solucion con la clase Lock.

    private boolean detener;

    public Immortal(String name, List<Immortal> immortalsPopulation, int health, int defaultDamageValue, ImmortalUpdateReportCallback ucb) {
        super(name);
        this.updateCallback = ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = health;
        this.defaultDamageValue = defaultDamageValue;
        pausar = false;
        lock = new Object();
        detener = false;
    }

    public void run() {

        while (!detener) {
            while (!pausar) {

                Immortal im;

                int myIndex = immortalsPopulation.indexOf(this);

                int nextFighterIndex = r.nextInt(immortalsPopulation.size());

                //avoid self-fight
                if (nextFighterIndex == myIndex) {
                    nextFighterIndex = ((nextFighterIndex + 1) % immortalsPopulation.size());
                }
                im = immortalsPopulation.get(nextFighterIndex);

                this.fight(im);

                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Immortal.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void fight(Immortal i2) {
        synchronized (i2) {
            if (i2.getHealth() > 0) {
                i2.changeHealth(i2.getHealth() - defaultDamageValue);
            } else {
                updateCallback.processReport(this + " says:" + i2 + " is already dead!\n");
            }
        }
        synchronized (this) {
            this.health += defaultDamageValue;
        }
        updateCallback.processReport("Fight: " + this + " vs " + i2 + "\n");
    }

    public void changeHealth(int v) {
        health = v;
        if (health == 0) {
            immortalsPopulation.remove(this);
            this.pausar();
        }
    }

    public void pausar() {
        pausar = true;
    }

    public void reanudar() {
        pausar = false;
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    public void detener() {
        pausar = true;
        detener = true;
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    public int getHealth() {
        return health;
    }

    @Override
    public String toString() {

        return name + "[" + health + "]";
    }

}
