package br.ufpa.cbcc.threads.entity;

public enum TipoNumero {

    CIVIL(1),
    MEDICO(2),
    BOMBEIRO(3);

    public static int numPrioridade;

    TipoNumero(int numPrioridade) { }

    public int getValue() {
        return numPrioridade;
    }

}
