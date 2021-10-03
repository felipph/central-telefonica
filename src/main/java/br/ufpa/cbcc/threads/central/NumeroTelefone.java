package br.ufpa.cbcc.threads.central;

public class NumeroTelefone {

    private TipoNumero tipoNumero;
    private Integer numTelefome;
    private NumeroTelefone emLicacaoCom;
    private Antena antenaRegistrada = null;

    public NumeroTelefone(TipoNumero tipoNumero, Integer numTelefome) {
        this.tipoNumero = tipoNumero;
        this.numTelefome = numTelefome;
    }

    public void ligarCom(NumeroTelefone emLicacaoCom) {
        this.emLicacaoCom = emLicacaoCom;
    }

    public void desligar() {
        NumeroTelefone telefoneEmLigacao = null;
    }

    public TipoNumero getTipoNumero() {
        return tipoNumero;
    }

    public boolean estaEmLigacao() {
        return this.emLicacaoCom != null;
    }

    public Integer getNumTelefome() {
        return numTelefome;
    }

    public Antena getAntenaRegistrada() {
        return antenaRegistrada;
    }

    public void setAntenaRegistrada(Antena antenaRegistrada) {
        this.antenaRegistrada = antenaRegistrada;
    }

    @Override
    public String toString() {

        Integer numeroConectado = null;
        if(emLicacaoCom != null) {
            numeroConectado = emLicacaoCom.getNumTelefome();
        }
        return "NumeroTelefone{" +
                "tipoNumero=" + tipoNumero +
                ", numTelefome='" + numTelefome + '\'' +
                ", emLicacaoCom=" + numeroConectado+
                '}';
    }

    public NumeroTelefone getNumeroConectado() {
        return emLicacaoCom;
    }
}
