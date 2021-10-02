package br.ufpa.cbcc.threads;

import br.ufpa.cbcc.threads.entity.Antena;
import br.ufpa.cbcc.threads.entity.CentralTelefonica;
import br.ufpa.cbcc.threads.entity.NumeroTelefone;
import br.ufpa.cbcc.threads.entity.TipoNumero;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;


public class Application {
    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        List<NumeroTelefone> numerosCriados = new ArrayList<>();

        for (int i = 0; i < 60; i++) {
            NumeroTelefone numeroTelefone = new NumeroTelefone(TipoNumero.CIVIL, 900000000+i);
            numerosCriados.add(numeroTelefone);
        }
        for (int i = 60; i < 90; i++) {
            NumeroTelefone numeroTelefone = new NumeroTelefone(TipoNumero.BOMBEIRO, 900000000+i);
            numerosCriados.add(numeroTelefone);
        }
        for (int i = 90; i < 100; i++) {
            NumeroTelefone numeroTelefone = new NumeroTelefone(TipoNumero.MEDICO, 900000000+i);
            numerosCriados.add(numeroTelefone);
        }

        Antena[] antenasCriadas = new Antena[2];

        antenasCriadas[0] = new Antena(2, "Antena-1");
        antenasCriadas[1] = new Antena(2, "Antena-2");

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

        LOG.info("Central: {}", central);

        List<NumeroTelefone> numeroRegistrados = new ArrayList<>(central.getTodosNumerosExistentes());

        LOG.info("Ligacao {} => {}", numeroRegistrados.get(0), numeroRegistrados.get(77));
        central.efetuarLigacao(numeroRegistrados.get(0), numeroRegistrados.get(77));
        LOG.info("Ligacao {} => {}", numeroRegistrados.get(2), numeroRegistrados.get(5));
        central.efetuarLigacao(numeroRegistrados.get(2), numeroRegistrados.get(5));

        List<Thread> threadsAntenas = central.iniciarAntenas();
        Thread.sleep(20000);



        central.desligarCentral();


        for (Thread threadsAntena : threadsAntenas) {
            threadsAntena.join();
        }


    }
}
