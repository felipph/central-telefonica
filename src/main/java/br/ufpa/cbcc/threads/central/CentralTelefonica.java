package br.ufpa.cbcc.threads.central;

import java.util.*;

public class CentralTelefonica {

    Set<NumeroTelefone> todosNumerosExistentes = new HashSet<>();
    List<Antena> antenas = new ArrayList<>();

    public void adicionarNumero(NumeroTelefone numeroTelefone, Antena antena) {
        if (todosNumerosExistentes.add(numeroTelefone)) {
            if (numeroTelefone.getAntenaRegistrada() == null) {
                if(antena.getNumerosRegistrados().containsKey(numeroTelefone.getNumTelefome())){
                    throw new IllegalArgumentException("Numero já registrado na antena: " + numeroTelefone + "\nAntena: "+antena);
                }
                antena.getNumerosRegistrados().put(numeroTelefone.getNumTelefome(), numeroTelefone);
                numeroTelefone.setAntenaRegistrada(antena);
                if(!antenas.contains(antena)) {
                    antenas.add(antena);
                }
            }
        } else {
            throw new IllegalArgumentException(String.format("Numero %s já registrado", numeroTelefone.getNumTelefome()));
        }
    }

    public List<Thread> iniciarAntenas() {
        List<Thread> threadsAntenas = new ArrayList<>();
        for (Antena antena : antenas) {
            Thread threadAntena = new Thread(antena);
            threadAntena.start();
            threadsAntenas.add(threadAntena);
        }
        return threadsAntenas;
    }

    public void desligarCentral() {
        for (Antena antena : this.antenas) {
            antena.pararAntena();
        }
    }

    public Set<NumeroTelefone> getTodosNumerosExistentes() {
        return todosNumerosExistentes;
    }

    public void efetuarLigacao(NumeroTelefone origem, NumeroTelefone destino) {
        for (Antena antena : antenas) {
            if(antena.getNumerosRegistrados().containsKey(destino.getNumTelefome())) {
                antena.efetuarLigacao(origem,destino);
            }
        }
    }

    public List<Antena> getAntenas() {
        return antenas;
    }

    public void desligar (NumeroTelefone numeroTelefone) {
        if(numeroTelefone.estaEmLigacao()) {
            Antena antenaRegistrada = numeroTelefone.getAntenaRegistrada();
            NumeroTelefone numeroConectado = numeroTelefone.getNumeroConectado();
            numeroConectado.getAntenaRegistrada().liberarSlot(numeroTelefone);
            antenaRegistrada.liberarSlot(numeroConectado);
            numeroTelefone.ligarCom(null);
            numeroConectado.ligarCom(null);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Total de antenas: ");
        sb.append(antenas.size());
        sb.append("Total de Numeros: ");
        sb.append( todosNumerosExistentes.size());
        sb.append("\n");
        sb.append("CentralTelefonica{\n");
        sb.append("\t antenas = {\n\t");
        sb.append(antenas);
        sb.append("\n}");
        return sb.toString();
    }
}
