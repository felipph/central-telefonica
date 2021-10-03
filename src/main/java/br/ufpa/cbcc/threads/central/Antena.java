package br.ufpa.cbcc.threads.central;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Antena implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(Antena.class);

    public boolean printStatus = false;

    public boolean printEspera = false;

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
    private AtomicInteger qntLigacoesAtuais = new AtomicInteger(0);
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

    public synchronized void retiraListaEspera() {
        if (numSlotsLigacao.compareTo(qntLigacoesAtuais.get()) > 0) {
            NumeroTelefone origemSelecionado = null;
            NumeroTelefone destinoSelecionado = null;
            Integer maiorPrioridade = 0;
            //aqui vai a logica para decidir qual tirar da lista
            for (Map.Entry<NumeroTelefone, NumeroTelefone> entry : ligacaoEmEspera.entrySet()) {
                NumeroTelefone origem = entry.getKey();
                NumeroTelefone destino = entry.getValue();
                Integer prioridade = origem.getTipoNumero().getValue() + destino.getTipoNumero().getValue();
                if (maiorPrioridade.compareTo(prioridade) < 0) {
                    destinoSelecionado = destino;
                    origemSelecionado = origem;
                    maiorPrioridade = prioridade;
                }
            }
            if (!ligacaoEmEspera.isEmpty()) {
                LOG.info("{} - FilaAtual: ", nomeAntena);
                ligacaoEmEspera.forEach((origem, destino) -> {
                    LOG.info("{} => {}({})", origem.getNumTelefome(), destino.getNumTelefome(), origem.getTipoNumero().getValue() + destino.getTipoNumero().getValue());
                });
            }
            LOG.info("{} - Numeros selecionados {} => {} - prioridade {}", nomeAntena, origemSelecionado.getNumTelefome(), destinoSelecionado.getNumTelefome(), maiorPrioridade);
            LOG.info("{} - Tipos selecionados {} => {} - prioridade {}", nomeAntena, origemSelecionado.getTipoNumero().name(), destinoSelecionado.getTipoNumero().name(), maiorPrioridade);
            assert origemSelecionado != null;
            ligacaoEmEspera.remove(origemSelecionado);
            efetuarLigacao(origemSelecionado, destinoSelecionado);
        }
    }

    public synchronized boolean efetuarLigacao(NumeroTelefone numeroSolicitante, NumeroTelefone numeroSolicitado) {
        if (!this.getNumerosRegistrados().containsKey(numeroSolicitado.getNumTelefome())) {
            return false;
        }
        if (this.numSlotsLigacao.compareTo(this.slotsEmUso.size()) > 0) {
            if (numeroSolicitado.estaEmLigacao()) {
                LOG.info("Numero Ocupado! {}", numeroSolicitado.getNumTelefome());
                return false;
            }
            if (numeroSolicitante.estaEmLigacao()) {
                LOG.info("O Numero de origem já está em ligação! {}", numeroSolicitante.getNumTelefome());
                return false;
            }
            this.slotsEmUso.put(numeroSolicitante, numeroSolicitado);
            qntLigacoesAtuais.incrementAndGet();
            numeroSolicitante.ligarCom(numeroSolicitado);
            ;
            numeroSolicitado.ligarCom(numeroSolicitante);
            LOG.info("{} - Ligacao conectada {} => {}", nomeAntena, numeroSolicitante.getNumTelefome(), numeroSolicitado.getNumTelefome());
            return true;

        } else {
            LOG.warn("{} - Ligacao adicioonada à espera {} => {}", nomeAntena, numeroSolicitante.getNumTelefome(), numeroSolicitado.getNumTelefome());
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

                if (printStatus) {
                    LOG.info("{}: Status: {}", this.nomeAntena, this.getStatusAntena());
                    printStatus = false;
                }

                if (printEspera) {
                    LOG.info("Ligações em espera");
                    this.ligacaoEmEspera.forEach((origem, destino) -> {
                        LOG.info("{} => {} ({})", origem.getNumTelefome(), destino.getNumTelefome(), origem.getTipoNumero().getValue() + destino.getTipoNumero().getValue());
                    });
                    printEspera = false;
                }

                if (!this.ligacaoEmEspera.isEmpty()) {
                    retiraListaEspera();
                }

            }
            LOG.info("Deligando a antena {}", this.nomeAntena);
        } else {
            LOG.error("A Antena não foi iniciada pois não tem numeros registrados!");
        }
    }

    public void printStatus() {
        this.printStatus = true;
    }

    public void printEspera() {
        this.printEspera = true;
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
        slotsEmUso.forEach((origem, destino) -> {
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

    public void liberarSlot(NumeroTelefone numeroTelefone) {
        NumeroTelefone destino = this.slotsEmUso.remove(numeroTelefone);
        if (destino != null) {
            this.qntLigacoesAtuais.decrementAndGet();
            LOG.info("{} - Ligação encerrada: {} => {}", nomeAntena, numeroTelefone.getNumTelefome(), destino.getNumTelefome());
        }
    }

    public void proximoListaEspera() {
        if (!ligacaoEmEspera.isEmpty()) {
            NumeroTelefone origemSelecionado = null;
            NumeroTelefone destinoSelecionado = null;
            Integer maiorPrioridade = 0;
            //aqui vai a logica para decidir qual tirar da lista
            for (Map.Entry<NumeroTelefone, NumeroTelefone> entry : ligacaoEmEspera.entrySet()) {
                NumeroTelefone origem = entry.getKey();
                NumeroTelefone destino = entry.getValue();
                Integer prioridade = origem.getTipoNumero().getValue() + destino.getTipoNumero().getValue();
                if (maiorPrioridade.compareTo(prioridade) < 0) {
                    destinoSelecionado = destino;
                    origemSelecionado = origem;
                    maiorPrioridade = prioridade;
                }
            }
            LOG.info("{} - {} => {}({})", nomeAntena, origemSelecionado.getNumTelefome(), destinoSelecionado.getNumTelefome(), origemSelecionado.getTipoNumero().getValue() + destinoSelecionado.getTipoNumero().getValue());

        } else {
            LOG.warn("{} - Não há ligacoes em espera", nomeAntena);
        }
    }
}
