package br.ufpa.cbcc.threads;

import br.ufpa.cbcc.threads.central.Antena;
import br.ufpa.cbcc.threads.central.CentralTelefonica;
import br.ufpa.cbcc.threads.central.NumeroTelefone;
import br.ufpa.cbcc.threads.central.TipoNumero;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.*;


public class Application {
    private static final Logger LOG = LoggerFactory.getLogger(Application.class.getName());

    private static final List<Discador> discadores = new ArrayList<>();
    private static final List<Thread> threadsDiscadores = new ArrayList<>();

    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        List<NumeroTelefone> numerosCriados = new ArrayList<>();
        CentralTelefonica central = setupCentral(numerosCriados);
        List<Thread> threadsCriadas = central.iniciarAntenas();



        Discador discador1 = new Discador(numerosCriados, central);
        Discador discador2 = new Discador(numerosCriados, central);

        Thread threadDiscador1 = new Thread(discador1);
        Thread threadDiscador2 = new Thread(discador2);
        threadsDiscadores.add(threadDiscador1);
        threadsDiscadores.add(threadDiscador2);
        discadores.add(discador1);
        discadores.add(discador2);

        LOG.info("Central: {}", central);

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String comando = reader.readLine();
            if (comando.equals("exit")) {
                discador1.kill();
                discador2.kill();
                break;
            }
            parseComando(comando,central);
        }
        central.desligarCentral();
        for (Thread threadsAntena : threadsCriadas) {
            threadsAntena.join();
        }
    }

    private static CentralTelefonica setupCentral(List<NumeroTelefone> numerosCriados) {
        for (int i = 0; i < 60; i++) {
            NumeroTelefone numeroTelefone = new NumeroTelefone(TipoNumero.CIVIL,    i);
            numerosCriados.add(numeroTelefone);
        }
        for (int i = 60; i < 90; i++) {
            NumeroTelefone numeroTelefone = new NumeroTelefone(TipoNumero.MEDICO,  i);
            numerosCriados.add(numeroTelefone);
        }
        for (int i = 90; i < 100; i++) {
            NumeroTelefone numeroTelefone = new NumeroTelefone(TipoNumero.BOMBEIRO,    i);
            numerosCriados.add(numeroTelefone);
        }

        Antena[] antenasCriadas = new Antena[3];

        antenasCriadas[0] = new Antena(2, "Antena-0");
        antenasCriadas[1] = new Antena(2, "Antena-1");
        antenasCriadas[2] = new Antena(2, "Antena-2");

        CentralTelefonica central = new CentralTelefonica();

        while (!numerosCriados.isEmpty()) {
            NumeroTelefone[] numerosRestantes = numerosCriados.toArray(new NumeroTelefone[0]);
            int indexNumero = new Random().nextInt(numerosRestantes.length);
            NumeroTelefone numeroTelefone = numerosRestantes[indexNumero];
            List<NumeroTelefone> numerosRestantesList = new ArrayList<>();
            for (int i = 0; i < numerosRestantes.length; i++) {
                if (i != indexNumero) {
                    numerosRestantesList.add(numerosRestantes[i]);
                }
            }
            numerosCriados = numerosRestantesList;
            int indexAntena = new Random().nextInt(antenasCriadas.length);
            central.adicionarNumero(numeroTelefone, antenasCriadas[indexAntena]);
        }
        return central;
    }

    private static void parseComando(String comando, CentralTelefonica centralTelefonica) {
        String[] partes = comando.split("[ ]");
        String cmd = partes[0];
        try {
            if (partes.length == 3) {
                Integer numeroSolicitante = Integer.parseInt(partes[1]);
                Integer numeroSolicitado = Integer.parseInt(partes[2]);
                if (cmd.equals("ligar")) {
                    NumeroTelefone numSolicitante = null;
                    NumeroTelefone numSolicitado = null;

                    for (NumeroTelefone numeroCentral : centralTelefonica.getTodosNumerosExistentes()) {
                        if (numeroSolicitante.equals(numeroCentral.getNumTelefome())) {
                            numSolicitante = numeroCentral;
                        }
                        if (numeroSolicitado.equals(numeroCentral.getNumTelefome())) {
                            numSolicitado = numeroCentral;
                        }
                        if (numSolicitante != null && numSolicitado != null) {
                            break;
                        }
                    }
                    if (numSolicitante == null || numSolicitado == null) {
                        LOG.error("Um dos numeros solicitados não foi encontrado!");
                    } else {
                        centralTelefonica.efetuarLigacao(numSolicitante, numSolicitado);
                    }
                }


                else {
                    LOG.error("Comando não encontrado '{}'", cmd);
                }
            } else if(partes.length == 2) {
                if(cmd.equals("status")) {
                    Integer indexAntena = Integer.parseInt(partes[1]);
                    Antena antena = centralTelefonica.getAntenas().get(indexAntena);
                    if(antena != null) {
                        antena.printStatus();
                    }
                }
                else if(cmd.equals("espera")) {
                    Integer indexAntena = Integer.parseInt(partes[1]);
                    Antena antena = centralTelefonica.getAntenas().get(indexAntena);
                    antena.printEspera();

                }
                else if(cmd.equals("proximo")) {
                    Integer indexAntena = Integer.parseInt(partes[1]);
                    Antena antena = centralTelefonica.getAntenas().get(indexAntena);
                    antena.proximoListaEspera();

                }
                else if(cmd.equals("desligar")) {
                    Integer numeroInformado = Integer.parseInt(partes[1]);
                    NumeroTelefone numSolicitante = null;
                    for (NumeroTelefone numeroCentral : centralTelefonica.getTodosNumerosExistentes()) {
                        if (numeroInformado.equals(numeroCentral.getNumTelefome())) {
                            numSolicitante = numeroCentral;
                        }
                    }
                    if(numSolicitante ==null) {
                        LOG.error("Numero inexistente! '{}'", numeroInformado);
                    } else {
                        centralTelefonica.desligar(numSolicitante);
                    }
                }
            }
            else {
                if(comando.equals("resumeDiscadores")) {
                    for (Discador discador : discadores) {
                        discador.resume();
                    }
                }
                else  if(comando.equals("pauseDiscadores")) {
                    for (Discador discador : discadores) {
                        discador.pause();
                    }
                }
                else  if(comando.equals("startDiscadores")) {
                    for (Thread discador : threadsDiscadores) {
                        discador.start();
                    }
                }

                else {
                    LOG.error("Comando não reconhecido: {}", comando);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
