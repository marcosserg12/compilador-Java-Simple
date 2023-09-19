import java.util.Arrays;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class AnaliseCodigo {
    
    private final String codigo;
    private final int tamanhoCodigo;
    private int indiceAtual;
    private Id idAtual;
    private Id idAnterior;

    private final Set<String> variaveisDeclaradas;
    private final Set<Integer> numerosDeLinha;
    private final Set<Integer> alvosGoto;
    private int ultimoNumeroLinha = 0;

    public AnaliseCodigo(String codigo) {
        this.codigo = codigo;
        this.indiceAtual = 0;
        this.tamanhoCodigo = codigo.length();
        this.variaveisDeclaradas = new HashSet<>();
        this.numerosDeLinha = new HashSet<>();
        this.alvosGoto = new HashSet<>();
    }

    public boolean proximoId() {
        while(!finalizado()) {
            this.idAnterior = this.idAtual;

            final char caractereAtual = codigo.charAt(indiceAtual);
            if(ConfiguracaoAnalise.CARACTERES_IGNORADOS.contains(caractereAtual)) {
                pularEspacosEmBranco();
                continue;
            } else if(caractereAtual == 'r' && (indiceAtual + 3 < tamanhoCodigo) && codigo.startsWith("rem", indiceAtual)) {
                idAtual = new Id(Simbolo.REM, "rem");
                indiceAtual += 4;
                pularComentario();
            } else if ((Character.isLetter(caractereAtual) && Character.isLowerCase(caractereAtual)) || ConfiguracaoAnalise.OPERADORES.contains(caractereAtual)) {
                Simbolo simbolo = lerSimbolo();
                if (simbolo == null) {
                    idAtual = new Id(Simbolo.VARIAVEL, String.valueOf(codigo.charAt(indiceAtual)));
                    indiceAtual++;
                } else {
                    idAtual = new Id(simbolo, simbolo.getSimbolo());
                }
            } else if (Character.isDigit(caractereAtual)) {
                idAtual = new Id(Simbolo.INTEIRO, lerInteiro());
            } else {
                throw new RuntimeException("Id não mapeado: " + caractereAtual);
            }
            return true;
        }
        return false;
    }

    public Id espiarProximoId() {
        int indiceTemporario = indiceAtual;
        Id idTemporarioAtual = idAtual;
        Id idTemporarioAnterior = idAnterior;

        proximoId();
        Id proximo = idAtual;

        indiceAtual = indiceTemporario;
        idAtual = idTemporarioAtual;
        idAnterior = idTemporarioAnterior;

        return proximo;
    }

    private Simbolo lerSimbolo() {
        if(ConfiguracaoAnalise.OPERADORES.contains(codigo.charAt(indiceAtual))) {
            return lerSimboloOperador();
        } else {
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < 5 && indiceAtual + i < tamanhoCodigo; i++) {
                sb.append(codigo.charAt(indiceAtual + i));
                Simbolo simbolo = Simbolo.parse(sb.toString());
                if(simbolo != null) {
                    indiceAtual += (i + 1);
                    return simbolo;
                }
            }
            return null;
        }
    }

    private Simbolo lerSimboloOperador() {
        char caractereAtual = codigo.charAt(indiceAtual);
        String potencialOperadorDuplo = String.valueOf(caractereAtual);

        if (indiceAtual + 1 < tamanhoCodigo && ConfiguracaoAnalise.OPERADORES.contains(codigo.charAt(indiceAtual + 1))) {
            char proximoCaractere = codigo.charAt(indiceAtual + 1);
            potencialOperadorDuplo += proximoCaractere;
            Simbolo simboloOperadorDuplo = Simbolo.parse(potencialOperadorDuplo);

            if (simboloOperadorDuplo != null) {
                indiceAtual += 2;
                return simboloOperadorDuplo;
            }
        }
        indiceAtual++;
        return Simbolo.parse(String.valueOf(caractereAtual));
    }

    private String lerInteiro() {
        StringBuilder sb = new StringBuilder();
        char caractereAtual = codigo.charAt(indiceAtual);

        while (!finalizado() && Character.isDigit(caractereAtual)) {
            sb.append(caractereAtual);
            indiceAtual++;
            if (finalizado()) break;
            caractereAtual = codigo.charAt(indiceAtual);
        }
        return sb.toString();
    }

    public void pularComentario() {
        while(!finalizado() && codigo.charAt(indiceAtual) != '\n') {
            indiceAtual++;
        }
    }

    public void pularEspacosEmBranco() {
        while(!finalizado()) {
            if(ConfiguracaoAnalise.CARACTERES_IGNORADOS.contains(codigo.charAt(indiceAtual))){
                this.indiceAtual++;
            } else {
                break;
            }
        }
    }

    public boolean finalizado() {
        return indiceAtual >= tamanhoCodigo;
    }

    public void analisarSintaticamente() {
        proximoId();
        while (idAtual.getSimbolo() != Simbolo.FIM) {
            System.out.printf("Analisando id: %s%n", idAtual.getSimbolo());
            if (proximoIdEhComando()) {
                numeroLinha();
            }
            comando();
        }
    }

    private boolean proximoIdEhComando() {
        Simbolo proximoSimbolo = espiarProximoId().getSimbolo();
        return proximoSimbolo == Simbolo.REM || proximoSimbolo == Simbolo.ENTRADA || proximoSimbolo == Simbolo.ATRIB ||
                proximoSimbolo == Simbolo.IMPRIMIR || proximoSimbolo == Simbolo.IRPARA || proximoSimbolo == Simbolo.SE ||
                proximoSimbolo == Simbolo.FIM;
    }

    private void numeroLinha() {
        if (idAtual.getSimbolo() != Simbolo.INTEIRO) {
            throw new RuntimeException("Esperado número de linha no início da expressão.");
        }
        int numeroLinhaAtual = Integer.parseInt(idAtual.getValor());
        if (numeroLinhaAtual <= ultimoNumeroLinha) {
            throw new RuntimeException("Número de linha " + numeroLinhaAtual + " não está em ordem crescente ou foi repetido.");
        }
        if (numerosDeLinha.contains(numeroLinhaAtual)) {
            throw new RuntimeException("Número de linha " + numeroLinhaAtual + " repetido.");
        }
        numerosDeLinha.add(numeroLinhaAtual);

        ultimoNumeroLinha = numeroLinhaAtual;
        proximoId();
    }



    private void comando() {
        switch (idAtual.getSimbolo()) {
            case REM:
                // Pula o comentário
                break;
            case ENTRADA:
                proximoId();
                declararVariavel(idAtual.getValor());
                proximoId();
                break;
            case ATRIB:
                proximoId();
                declararVariavel(idAtual.getValor());
                proximoId();
                proximoId(); // Pula ATRIBUICAO
                expressao();
                break;
            case SE:
                proximoId();
                condicao();
                if (idAtual.getSimbolo() != Simbolo.IRPARA) {
                    throw new RuntimeException("Esperado GOTO após condição.");
                }
                proximoId();
                if (idAtual.getSimbolo() != Simbolo.INTEIRO) {
                    throw new RuntimeException("Número de linha esperado após GOTO.");
                }
                alvosGoto.add(Integer.parseInt(idAtual.getValor()));
                proximoId();
                break;
            case  IRPARA:
                proximoId();
                if (idAtual.getSimbolo() != Simbolo.INTEIRO) {
                    throw new RuntimeException("Número de linha esperado após GOTO.");
                }
                alvosGoto.add(Integer.parseInt(idAtual.getValor()));
                proximoId();
                break;
            case IMPRIMIR:
                proximoId();
                usarVariavel(idAtual.getValor());
                proximoId();
                break;
            case FIM:
                // Não faz nada.
                break;
            default:
                throw new RuntimeException("Comando não reconhecido.");
        }
    }

    private void expressao() {
        termo();
        while (idAtual.getSimbolo() == Simbolo.ADICAO || idAtual.getSimbolo() == Simbolo.SUBTRACAO) {
            proximoId();
            termo();
        }
    }

    private void termo() {
        fator();
        while (idAtual.getSimbolo() == Simbolo.MULTIPLICACAO || idAtual.getSimbolo() == Simbolo.DIVISAO) {
            proximoId();
            fator();
        }
    }

    private void fator() {
        if (idAtual.getSimbolo() == Simbolo.VARIAVEL) {
            usarVariavel(idAtual.getValor());
            proximoId();
        } else if (idAtual.getSimbolo() == Simbolo.INTEIRO) {
            proximoId();
        } else {
            throw new RuntimeException("Fator não reconhecido.");
        }
    }

    private void condicao() {
        expressao();
        if (idAtual.getSimbolo() == Simbolo.IGUAL || idAtual.getSimbolo() == Simbolo.DIFERENTE ||
                idAtual.getSimbolo() == Simbolo.MAIOR || idAtual.getSimbolo() == Simbolo.MENOR ||
                idAtual.getSimbolo() == Simbolo.MAIOR_OU_IGUAL || idAtual.getSimbolo() == Simbolo.MENOR_OU_IGUAL) {
            proximoId();
            expressao();
        } else {
            throw new RuntimeException("Operador de condição não reconhecido.");
        }
    }

    private void declararVariavel(String nome) {
        if (!variaveisDeclaradas.contains(nome)) {
            variaveisDeclaradas.add(nome);
        } else {
            throw new RuntimeException("Variável " + nome + " já foi declarada.");
        }
    }

    private void usarVariavel(String nome) {
        if (!variaveisDeclaradas.contains(nome)) {
            throw new RuntimeException("Variável " + nome + " não foi declarada.");
        }
    }

    // ... [Outros métodos auxiliares traduzidos]

    public void analisarSemanticamente() {
        proximoId();
        while (idAtual.getSimbolo() != Simbolo.FIM) {
            System.out.println(String.format("Analisando id: %s", idAtual.getSimbolo()));
            numeroLinha();
            comando();
        }
        verificarAlvosGoto();
    }

    // ... [Outros métodos auxiliares traduzidos]

    private void verificarAlvosGoto() {
        for (int alvo : alvosGoto) {
            if (!numerosDeLinha.contains(alvo)) {
                throw new RuntimeException("GOTO para linha " + alvo + " que não existe.");
            }
        }
    }
}
