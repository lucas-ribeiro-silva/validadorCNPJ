// src/main/java/com/minhaempresa/validador/Main.java
package com.minhaempresa.validador;

import javax.swing.*;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.SocketTimeoutException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.JsonElement;

/**
 * **Validador de CNPJ (Java Swing)**
 *
 * <p>Esta classe representa a aplicação principal do Validador de CNPJ,
 * fornecendo uma interface gráfica de usuário (GUI) desenvolvida com Java Swing.
 * A aplicação permite ao usuário inserir um CNPJ, validar sua conformidade matemática
 * e, em seguida, consultar dados cadastrais da empresa através da API pública da ReceitaWS.</p>
 *
 * <p>Demonstra o uso de:</p>
 * <ul>
 * <li>Componentes Swing para construção de GUI.</li>
 * <li>Requisições HTTP assíncronas com OkHttp para não travar a UI.</li>
 * <li>Parsing de respostas JSON com Google Gson.</li>
 * <li>Validação de dados (algoritmo de CNPJ).</li>
 * <li>Gerenciamento de dependências com Maven.</li>
 * </ul>
 */
public class Main extends JFrame {

    // --- Componentes da Interface Gráfica ---
    private JLabel labelCnpj;
    private JTextField campoCnpj;
    private JButton botaoValidar;
    private JTextArea areaResultado;
    private JScrollPane scrollPane;

    // --- Clientes para Operações de Rede e JSON ---
    private OkHttpClient httpClient;
    private Gson gson;

    /**
     * Construtor da classe `Main`.
     * <p>Inicializa e configura todos os componentes da interface gráfica do usuário (GUI),
     * define as propriedades da janela (título, tamanho, operação de fechamento, layout)
     * e configura os ouvintes de eventos para os botões.</p>
     * <p>Também inicializa o cliente HTTP (`OkHttpClient`) e o parser JSON (`Gson`)
     * para futuras requisições de API.</p>
     */
    public Main() {
        setTitle("Validador de CNPJ");
        setSize(500, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

        labelCnpj = new JLabel("CNPJ:");
        campoCnpj = new JTextField(18);
        botaoValidar = new JButton("Validar CNPJ");
        areaResultado = new JTextArea(17, 40);
        areaResultado.setEditable(false);
        scrollPane = new JScrollPane(areaResultado);

        add(labelCnpj);
        add(campoCnpj);
        add(botaoValidar);
        add(scrollPane);

        httpClient = new OkHttpClient.Builder()
                .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS) // Define um tempo limite de leitura de 10 segundos
                .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS) // Define um tempo limite de conexão de 10 segundos
                .build();
        gson = new Gson();

        // Adiciona um ActionListener ao botão "Validar CNPJ"
        botaoValidar.addActionListener(new ActionListener() {
            /**
             * Manipula o evento de clique no botão "Validar CNPJ".
             * <p>Ao clicar, uma nova Thread é iniciada para executar a lógica de validação e consulta da API.
             * Isso é crucial para evitar que a interface gráfica fique "congelada" (não responsiva)
             * enquanto a requisição de rede está sendo processada.</p>
             * @param e O evento de ação que disparou este método.
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(() -> validarEConsultarCnpj()).start();
            }
        });
    }

    /**
     * Orquestra o processo de validação matemática do CNPJ e, se bem-sucedida,
     * inicia a consulta à API externa.
     * <p>Este método é executado em uma Thread separada da Event Dispatch Thread (EDT)
     * para manter a responsividade da GUI.</p>
     * <p>Realiza os seguintes passos:</p>
     * <ol>
     * <li>Limpa a área de resultados na GUI.</li>
     * <li>Obtém o CNPJ digitado e remove caracteres não numéricos.</li>
     * <li>Verifica se o CNPJ está vazio ou tem um tamanho inválido.</li>
     * <li>Chama {@link #isCnpjValid(String)} para a validação matemática.</li>
     * <li>Se válido, chama {@link #consultarCnpjNaReceitaWS(String)} para a consulta externa.</li>
     * <li>Atualiza a área de resultados com as mensagens de validação.</li>
     * </ol>
     */
    private void validarEConsultarCnpj() {
        String cnpjDigitado = campoCnpj.getText();
        // Garante que a atualização da GUI ocorra na EDT
        SwingUtilities.invokeLater(() -> areaResultado.setText(""));

        String cnpjNumerico = cnpjDigitado.replaceAll("[^0-9]", "");

        if (cnpjNumerico.isEmpty()) {
            SwingUtilities.invokeLater(() -> areaResultado.append("Por favor, digite um CNPJ.\n"));
            return;
        }

        if (cnpjNumerico.length() != 14) {
            SwingUtilities.invokeLater(() -> areaResultado.append("CNPJ deve ter 14 dígitos.\n"));
            return;
        }

        if (isCnpjValid(cnpjNumerico)) {
            SwingUtilities.invokeLater(() -> areaResultado.append("CNPJ " + cnpjDigitado + " é matematicamente válido.\n"));
            consultarCnpjNaReceitaWS(cnpjNumerico);
        } else {
            SwingUtilities.invokeLater(() -> areaResultado.append("CNPJ " + cnpjDigitado + " é matematicamente inválido.\n"));
            SwingUtilities.invokeLater(() -> areaResultado.append("Por favor, verifique os dígitos e tente novamente.\n"));
        }
    }

    /**
     * Consulta a API pública da ReceitaWS para obter dados cadastrais de um CNPJ.
     * <p>Este método faz uma requisição HTTP GET para a API e processa a resposta JSON,
     * extraindo informações como Razão Social, Nome Fantasia, Endereço e Status na Receita Federal.</p>
     * <p>Todos os updates na interface gráfica são feitos via `SwingUtilities.invokeLater()`
     * para garantir a segurança da Thread.</p>
     *
     * @param cnpj O CNPJ a ser consultado, fornecido como uma String contendo apenas os 14 dígitos numéricos.
     */
    private void consultarCnpjNaReceitaWS(String cnpj) {
        String url = "https://www.receitaws.com.br/v1/cnpj/" + cnpj;
        Request request = new Request.Builder().url(url).build();

        SwingUtilities.invokeLater(() -> areaResultado.append("Consultando ReceitaWS para " + cnpj + "...\n"));

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                SwingUtilities.invokeLater(() -> areaResultado.append("Requisição falhou: " + response.code() + " - " + response.message() + "\n"));
                return;
            }

            String responseBody = response.body().string();

            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

            // Verifica se a API retornou um erro (status "ERROR" no JSON)
            if (jsonResponse.has("status") && "ERROR".equals(jsonResponse.get("status").getAsString())) {
                String mensagemErro = getStringOrDefault(jsonResponse, "message", "Erro desconhecido da API.");
                SwingUtilities.invokeLater(() -> {
                    areaResultado.append("Erro na consulta da API: " + mensagemErro + "\n");
                    areaResultado.append("Por favor, tente novamente mais tarde ou verifique o CNPJ.\n");
                });
                return;
            }

            // Extrai os dados do JSON usando o método auxiliar getStringOrDefault
            String nomeFantasia = getStringOrDefault(jsonResponse, "fantasia", "Não informado");
            String razaoSocial = getStringOrDefault(jsonResponse, "nome", "Não informado");
            String status = getStringOrDefault(jsonResponse, "situacao", "Não informado"); // Ex: ATIVA, BAIXADA, NULA
            String endereco = getStringOrDefault(jsonResponse, "logradouro", "");
            String numero = getStringOrDefault(jsonResponse, "numero", "");
            String complemento = getStringOrDefault(jsonResponse, "complemento", "");
            String bairro = getStringOrDefault(jsonResponse, "bairro", "");
            String cidade = getStringOrDefault(jsonResponse, "municipio", "");
            String uf = getStringOrDefault(jsonResponse, "uf", "");
            String cep = getStringOrDefault(jsonResponse, "cep", "");
            String inscricaoEstadual = "Não disponível na ReceitaWS (API pública)"; // A ReceitaWS não fornece a IE

            // Atualiza a interface gráfica com os resultados formatados
            SwingUtilities.invokeLater(() -> {
                areaResultado.append("\n--- Dados da Empresa (ReceitaWS) ---\n");
                areaResultado.append("CNPJ: " + cnpj + "\n");
                areaResultado.append("Razão Social: " + razaoSocial + "\n");
                areaResultado.append("Nome Fantasia: " + nomeFantasia + "\n");
                areaResultado.append("Inscrição Estadual: " + inscricaoEstadual + "\n");
                areaResultado.append("Status na Receita: " + status + "\n");
                areaResultado.append("Endereço: " + endereco + ", " + numero + (complemento.isEmpty() ? "" : " - " + complemento) + "\n");
                areaResultado.append("Bairro: " + bairro + "\n");
                areaResultado.append("Cidade/UF: " + cidade + "/" + uf + "\n");
                areaResultado.append("CEP: " + cep + "\n");
                areaResultado.append("------------------------\n");

                if ("ATIVA".equalsIgnoreCase(status)) {
                    areaResultado.append("Status Final: CNPJ está **ATIVO** na Receita Federal.\n");
                } else {
                    areaResultado.append("Status Final: CNPJ está **" + status + "** na Receita Federal. Pode ser inválido para operações.\n");
                }
            });

        } catch (SocketTimeoutException e) {
            SwingUtilities.invokeLater(() -> areaResultado.append("Erro de conexão: Tempo limite excedido ao consultar a API. Verifique sua internet.\n"));
            System.err.println("Socket Timeout: " + e.getMessage());
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> areaResultado.append("Erro ao consultar a API: " + e.getMessage() + "\n"));
            System.err.println("IOException: " + e.getMessage());
        } catch (JsonSyntaxException e) {
            SwingUtilities.invokeLater(() -> areaResultado.append("Erro ao processar a resposta da API (JSON inválido).\n"));
            System.err.println("JsonSyntaxException: " + e.getMessage());
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> areaResultado.append("Ocorreu um erro inesperado: " + e.getMessage() + "\n"));
            System.err.println("Erro inesperado: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Método auxiliar para obter um valor String de um {@link JsonObject} de forma segura.
     * <p>Verifica se a chave existe e se o valor não é nulo antes de tentar extraí-lo como String.
     * Caso a chave esteja ausente ou o valor seja nulo, retorna um valor padrão.</p>
     *
     * @param jsonObject O {@link JsonObject} do qual se deseja extrair o valor.
     * @param key A chave (nome do campo) a ser buscada no JSON.
     * @param defaultValue O valor padrão a ser retornado se a chave não for encontrada ou se seu valor for nulo.
     * @return O valor da chave como String, ou o `defaultValue` se não encontrado ou nulo.
     */
    private String getStringOrDefault(JsonObject jsonObject, String key, String defaultValue) {
        if (jsonObject.has(key)) {
            JsonElement element = jsonObject.get(key);
            if (!element.isJsonNull()) {
                return element.getAsString();
            }
        }
        return defaultValue;
    }

    /**
     * Valida matematicamente um número de CNPJ.
     * <p>Este método implementa o algoritmo de cálculo dos dígitos verificadores do CNPJ
     * para garantir sua conformidade matemática, antes de qualquer consulta externa.
     * Ele verifica o formato, a sequência de dígitos repetidos e os dois dígitos verificadores.</p>
     *
     * @param cnpj O CNPJ a ser validado, fornecido como uma String contendo apenas os 14 dígitos numéricos.
     * Caracteres não numéricos devem ser removidos antes de chamar este método.
     * @return `true` se o CNPJ for matematicamente válido de acordo com o algoritmo, `false` caso contrário.
     */
    public static boolean isCnpjValid(String cnpj) {
        // Verifica se o CNPJ tem 14 dígitos e se todos são números
        if (cnpj == null || !cnpj.matches("\\d{14}")) {
            return false;
        }

        // Verifica se todos os dígitos são iguais (ex: "00.000.000/0000-00"), o que o torna inválido
        if (cnpj.matches("(\\d)\\1{13}")) {
            return false;
        }

        // Array com os pesos para o cálculo do primeiro dígito verificador
        int[] peso1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        // Array com os pesos para o cálculo do segundo dígito verificador
        int[] peso2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        // Calcula o primeiro dígito verificador
        int soma = 0;
        for (int i = 0; i < 12; i++) {
            soma += Character.getNumericValue(cnpj.charAt(i)) * peso1[i];
        }
        int digito1 = 11 - (soma % 11);
        if (digito1 > 9) {
            digito1 = 0;
        }

        // Verifica o primeiro dígito
        if (Character.getNumericValue(cnpj.charAt(12)) != digito1) {
            return false;
        }

        // Calcula o segundo dígito verificador
        soma = 0; // Reinicia a soma
        for (int i = 0; i < 13; i++) {
            soma += Character.getNumericValue(cnpj.charAt(i)) * peso2[i];
        }
        int digito2 = 11 - (soma % 11);
        if (digito2 > 9) {
            digito2 = 0;
        }

        // Verifica o segundo dígito
        return Character.getNumericValue(cnpj.charAt(13)) == digito2;
    }

    /**
     * Método principal que inicia a aplicação.
     * <p>Cria uma instância da janela principal da aplicação e a torna visível.
     * Utiliza {@link SwingUtilities#invokeLater(Runnable)} para garantir que a
     * criação e inicialização da GUI ocorram na Event Dispatch Thread (EDT) do Swing,
     * que é a boa prática para evitar problemas de concorrência e garantir a estabilidade da UI.</p>
     *
     * @param args Argumentos de linha de comando (não utilizados nesta aplicação).
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Main().setVisible(true);
            }
        });
    }
}