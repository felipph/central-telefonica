package br.ufpa.cbcc.threads;

import br.ufpa.cbcc.threads.central.CentralTelefonica;
import br.ufpa.cbcc.threads.central.NumeroTelefone;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Discador implements Runnable{

    private final List<NumeroTelefone> numerosDisponiveis;

    private final CentralTelefonica centralTelefonica;

    private List<NumeroTelefone> numerosDiscados = new ArrayList<>();

    private boolean kill = false;

    private final Object pauseLock = new Object();

    private boolean paused = false;

    public void kill() {
        this.kill = true;
    }

    public Discador(List<NumeroTelefone> numerosDisponiveis, CentralTelefonica centralTelefonica) {
        assert numerosDisponiveis != null;
        this.numerosDisponiveis = numerosDisponiveis;
        this.centralTelefonica = centralTelefonica;
    }

    @Override
    public void run() {
        while(true) {
            if(paused) {
                try {
                    synchronized (pauseLock) {
                        pauseLock.wait();
                    }
                } catch (InterruptedException e) {
                    break;
                }
            } else {

            }
            if(kill) {
                break;
            }
            Integer opcao = new Random().nextInt(100)   ;
            if (opcao < 70) {

                NumeroTelefone[] numerosRestantes = numerosDisponiveis.toArray(new NumeroTelefone[0]);
                int indexNumero1 = new Random().nextInt(numerosRestantes.length);
                int indexNumero2 = new Random().nextInt(numerosRestantes.length);
                centralTelefonica.efetuarLigacao(numerosRestantes[indexNumero1], numerosRestantes[indexNumero2]);

                numerosDiscados.add(numerosRestantes[indexNumero1]);
            } else {
                if (!numerosDiscados.isEmpty()) {
                    NumeroTelefone numeroDiscado = numerosDiscados.remove(0);
                    centralTelefonica.desligar(numeroDiscado);
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public void pause() {
        this.paused = true;
    }

    public void resume() {
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll(); // Unblocks thread
        }
    }
}
