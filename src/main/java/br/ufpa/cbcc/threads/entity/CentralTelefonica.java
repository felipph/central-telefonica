package br.ufpa.cbcc.threads.entity;

import java.util.*;

public class CentralTelefonica {

    Set<NumeroTelefone> todosNumerosExistentes = new HashSet<>();
    Set<Antena> antenas = new HashSet<>();

    public void adicionarNumero(NumeroTelefone numeroTelefone, Antena antena) {
        if (todosNumerosExistentes.add(numeroTelefone)) {
            if (numeroTelefone.getAntenaRegistrada() == null) {
                if(antena.getNumerosRegistrados().containsKey(numeroTelefone.getNumTelefome())){
                    throw new IllegalArgumentException("Numero já registrado na antena: " + numeroTelefone + "\nAntena: "+antena);
                }
                antena.getNumerosRegistrados().put(numeroTelefone.getNumTelefome(), numeroTelefone);
                numeroTelefone.setAntenaRegistrada(antena);
                antenas.add(antena);
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
