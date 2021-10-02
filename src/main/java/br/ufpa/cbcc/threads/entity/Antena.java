package br.ufpa.cbcc.threads.entity;

import br.ufpa.cbcc.threads.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Antena implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(Antena.class);

    private boolean pararAntena = false;

    private String nomeAntena;

    /**
     * dita a quantidade de ligacoes simultaneas na antena
     */
    private final Integer numSlotsLigacao;

    /**
     * Lista de numeros registraddos
     */
    Map<Integer, NumeroTelefone> numerosRegistrados = new HashMap<>();
    /**
     * Quantidade de licagoes em curso;
     */
    private AtomicInteger qntLigacoesAtuais;
    /**
     * irá conter a  lista de todas as ligações em curso 2 vezes,
     * numero1 -> numero2
     */
    ConcurrentHashMap<NumeroTelefone, NumeroTelefone> slotsEmUso = new ConcurrentHashMap<>();
    /**
     * Fila espera
     */
    ConcurrentHashMap<NumeroTelefone, NumeroTelefone> ligacaoEmEspera = new ConcurrentHashMap<>();

    public Antena(Integer numSlotsLigacao, String nomeAntena) {
        this.numSlotsLigacao = numSlotsLigacao;
        this.nomeAntena = nomeAntena;
    }

    public void retiraListaEspera() {
        if (numSlotsLigacao.compareTo(qntLigacoesAtuais.get()) > 0) {
            NumeroTelefone numeroSolicitanteSelecionado = null;
            NumeroTelefone numeroSolicitadoSelecionado = null;
            Integer maiorPrioridade = 0;
            //aqui vai a logica para decidir qual tirar da lista
            for (Map.Entry<NumeroTelefone, NumeroTelefone> entry : ligacaoEmEspera.entrySet()) {
                NumeroTelefone numeroSolicitante = entry.getKey();
                NumeroTelefone numeroSolicitado = entry.getValue();
                Integer prioridade = numeroSolicitado.getTipoNumero().getValue() + numeroSolicitado.getTipoNumero().getValue();
                if (maiorPrioridade.compareTo(prioridade) < 0) {
                    numeroSolicitadoSelecionado = numeroSolicitado;
                    numeroSolicitanteSelecionado = numeroSolicitante;
                    maiorPrioridade = prioridade;
                }
            }
            LOG.info("FilaAtual: {}", ligacaoEmEspera);
            LOG.info("Numeros selecionados {} => {} - prioridade {}", numeroSolicitanteSelecionado, numeroSolicitadoSelecionado, maiorPrioridade);
            assert numeroSolicitanteSelecionado != null;
            ligacaoEmEspera.remove(numeroSolicitanteSelecionado);
            efetuarLigacao(numeroSolicitanteSelecionado, numeroSolicitadoSelecionado);
        }
    }

    public synchronized boolean efetuarLigacao(NumeroTelefone numeroSolicitante, NumeroTelefone numeroSolicitado) {
        if (!this.getNumerosRegistrados().containsKey(numeroSolicitado.getNumTelefome())) {
            return false;
        }
        if (this.numSlotsLigacao.compareTo(this.slotsEmUso.size()) > 0) {
            if (!numeroSolicitado.estaEmLigacao()) {
                this.slotsEmUso.put(numeroSolicitante, numeroSolicitado);
                LOG.info("Ligacao conectada {} => {}", numeroSolicitante, numeroSolicitado);
                return true;
            } else {
                LOG.info("Numero Ocupado! {}", numeroSolicitado);
                return false;
            }
        } else {
            this.ligacaoEmEspera.put(numeroSolicitante, numeroSolicitado);
        }
        return false;
    }

    public Map<Integer, NumeroTelefone> getNumerosRegistrados() {
        return numerosRegistrados;
    }

    @Override
    public void run() {
        if (!this.getNumerosRegistrados().isEmpty()) {
            LOG.info("Antena {} em execucao", this.nomeAntena);
            while (!pararAntena) {
                try {
                    if (this.ligacaoEmEspera.isEmpty()) {
                        Thread.sleep(2000);
                        LOG.info("Antena {}: nenhuma chamanda chegou =/", this.nomeAntena);
                        LOG.info("Status: {}", this.getStatusAntena());
                    } else {
                        retiraListaEspera();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            LOG.info("Deligando a antena {}", this.nomeAntena);
        } else {
            LOG.error("A Antena não foi iniciada pois não tem numeros registrados!");
        }
    }

    public void pararAntena() {
        this.pararAntena = true;
    }

    public String getNomeAntena() {
        return nomeAntena;
    }

    public String getStatusAntena() {
        StringBuilder sb = new StringBuilder();
        sb.append("Total de ligacoes conectadas: ");
        sb.append(slotsEmUso.size());
        sb.append("\n");
        sb.append("Total de ligacoes em espera: ");
        sb.append(ligacaoEmEspera.size());
        sb.append("\n");

        sb.append("Lista em curso: \n");
        slotsEmUso.forEach((origem,destino) -> {
            sb.append(origem.getNumTelefome());
            sb.append("(");
            sb.append(origem.getAntenaRegistrada().getNomeAntena());
            sb.append(")");
            sb.append(" => ");
            sb.append(destino.getNumTelefome());
            sb.append("(");
            sb.append(destino.getAntenaRegistrada().getNomeAntena());
            sb.append(")");
            sb.append("\n");
        });
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Antena: ");
        sb.append(this.nomeAntena);
        sb.append("\n");
        sb.append("Total de numeros da Antena: ");
        sb.append(numerosRegistrados.size());
        sb.append("\n");
        for (NumeroTelefone nume : numerosRegistrados.values()) {
            sb.append(nume);
            sb.append("\n");
        }
        return sb.toString();
    }
}
