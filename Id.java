public class Id {

    private final Simbolo simbolo;

    private String valor;

    public Id(Simbolo simbolo, String valor) {
        this.simbolo = simbolo;
        this.valor = valor;
    }

    public Id(Simbolo simbolo) {
        this.simbolo = simbolo;
    }

    public Simbolo getSimbolo() {
        return simbolo;
    }

    public String getValor() {
        return valor;
    }
}
