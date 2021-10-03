package br.ufpa.cbcc.threads.central;

public enum TipoNumero {

    CIVIL(1),
    MEDICO(2),
    BOMBEIRO(3);

    public final int numPrioridade;

    TipoNumero(int numPrioridade) {
        this.numPrioridade = numPrioridade;
    }

    public int getValue() {
        return numPrioridade;
    }

}
