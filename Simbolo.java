import java.util.HashMap;
import java.util.Map;

public enum Simbolo {
    ADICAO("+"),
    ATRIB("let"),
    ATRIBUICAO("="),
    DIVISAO("/"),
    DIFERENTE("!="),
    ENTRADA("input"),
    ERRO(""),
    FIM("end"),
    IGUAL("=="),
    IMPRIMIR("print"),
    INTEIRO(""),
    IRPARA("goto"),
    MAIOR(">"),
    MAIOR_OU_IGUAL(">="),
    MENOR("<"),
    MENOR_OU_IGUAL("<="),
    MODULO("%"),
    MULTIPLICACAO("*"),
    REM("rem"),
    SE("if"),
    SUBTRACAO("-"),
    VARIAVEL("");

    private static final Map<String, Simbolo> tipo = new HashMap<>();

    static {
        for(Simbolo simbolo : Simbolo.values()) {
            tipo.put(simbolo.getSimbolo(), simbolo);
        }
    }

    private final String simbolo;

    Simbolo(String simbolo)
    {
        this.simbolo = simbolo;
    }

    public static Simbolo parse(final String caractere) {
        return tipo.get(caractere);
    }

    public String getSimbolo() {
        return simbolo;
    }
}
