import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.stream.Collectors;

public class Principal {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Por favor, forneça o caminho do arquivo como argumento.");
            return;
        }

        String caminhoArquivo = args[0];
        String codigoFonte = carregarCodigoFonte(caminhoArquivo);

        if (codigoFonte.isEmpty()) {
            System.err.println("Erro ao carregar o código fonte.");
            return;
        }

        System.out.println("=== Início da Análise do Código ===");

        AnaliseCodigo analisador = new AnaliseCodigo(codigoFonte);
        while (analisador.proximoId()) {
            Id idAtual = analisador.espiarProximoId();
            System.out.println("Analisando id:" +  idAtual.getSimbolo());
        }

        System.out.println("\n--- Análise Sintática ---");
        try {
            System.out.println("Analisando...");
            analisador.analisarSintaticamente();
            System.out.println("Análise sintática realizada com êxito!");
        } catch (RuntimeException erro) {
            System.err.printf("Falha na análise sintática: %s%n", erro.getMessage());
            return;
        }

        System.out.println("\n--- Análise Semântica ---");
        try {
            System.out.println("Analisando...");
            analisador.analisarSemanticamente();
            System.out.println("Análise semântica finalizada com sucesso!");
        } catch (RuntimeException erro) {
            System.err.printf("Falha na análise semântica: %s%n", erro.getMessage());
        }

        System.out.println("\n=== Fim da Análise ===");
    }

    private static String carregarCodigoFonte(String caminho) {
        StringBuilder codigo = new StringBuilder();
        try (BufferedReader leitor = new BufferedReader(new FileReader(caminho))) {
            codigo.append(leitor.lines().collect(Collectors.joining(System.lineSeparator())));
        } catch (IOException e) {
            System.err.printf("Erro ao ler o arquivo: %s%n", e.getMessage());
        }
        return codigo.toString();
    }
}
