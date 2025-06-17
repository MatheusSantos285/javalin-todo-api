package br.com.notes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Cliente de linha de comando para interagir com a API de Tarefas.
 * Esta aplicação é independente e simula um consumidor da API.
 */
public class ApiClient {
    private static final String SERVER_URL = "http://localhost:7000";
    private static final String AUTH_TOKEN = "vasco-da-gama"; // Token para autenticação na API
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=== Cliente da API de Tarefas ===");

        // Mantém o cliente rodando em loop para permitir múltiplas requisições.
        while (true) {
            try {
                mostrarMenu();
            } catch (Exception e) {
                System.err.println("Erro ao processar a requisição: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Exibe o menu de opções e processa a escolha do usuário.
     */
    private static void mostrarMenu() {
        System.out.println("\nDigite a requisição que deseja enviar:");
        System.out.println("1. POST /tarefas - Criar uma nova tarefa");
        System.out.println("2. GET /tarefas - Listar todas as tarefas");
        System.out.println("3. GET /tarefas/{id} - Buscar tarefa por ID");
        System.out.println("4. GET /status");

        System.out.println("Sua escolha: ");
        int opcao = scanner.nextInt();
        scanner.nextLine();

        switch (opcao) {
            case 1:
                System.out.println("Você escolheu: POST /tarefas");
                postTarefas();
                break;
            case 2:
                System.out.println("Você escolheu: GET /tarefas");
                getTarefas();
                break;
            case 3:
                System.out.println("Você escolheu: GET /tarefas/{id}");
                System.out.print("Digite o ID da tarefa: ");
                int id = scanner.nextInt();
                getTarefaById(id);
                break;
            case 4:
                System.out.println("Você escolheu: GET /status");
                getStatus();
                break;
            default:
                System.out.println("Opção inválida.");
                break;
        }
    }

    /**
     * Coleta os dados do usuário e monta a requisição para criar uma nova tarefa.
     */
    private static void postTarefas() {
        System.out.println("Digite o título da tarefa:");
        String titulo = scanner.nextLine();
        System.out.println("Digite a descrição da tarefa:");
        String descricao = scanner.nextLine();

        // Cria o corpo da requisição em formato JSON.
        String jsonPayload = String.format("{\"titulo\":\"%s\",\"descricao\":\"%s\"}",
                titulo.replace("\"", "\\\""),
                descricao.replace("\"", "\\\""));

        enviarRequisicao("/tarefas", "POST", jsonPayload);
    }

    // Métodos de atalho para as requisições GET.
    private static void getTarefas() {
        enviarRequisicao("/tarefas", "GET", null);

    }

    private static void getTarefaById(int id) {
        enviarRequisicao("/tarefas/" + id, "GET", null);
    }

    private static void getStatus() {
        enviarRequisicao("/status", "GET", null);
    }

    /**
     * Método central que lida com o envio de todas as requisições HTTP
     * e com o processamento das respostas.
     * @param endpoint O caminho do recurso (ex: "/tarefas").
     * @param method   O método HTTP a ser usado (ex: "GET", "POST").
     * @param jsonPayload O corpo da requisição em JSON (usado apenas para POST/PUT).
     */
    private static void enviarRequisicao(String endpoint, String method, String jsonPayload) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(SERVER_URL + endpoint);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", AUTH_TOKEN);

            // Se for POST ou PUT, configura e envia o corpo da requisição.
            if ("POST".equals(method) || "PUT".equals(method)) {
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setDoOutput(true);

                System.out.println("\nEnviando requisição " + method + " para: " + url);
                System.out.println("Corpo da requisição: " + jsonPayload);
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
            }
            else {
                System.out.println("\nEnviando requisição " + method + " para: " + url);
            }

            // Lê a resposta do servidor.
            int responseCode = connection.getResponseCode();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    responseCode >= 200 && responseCode <= 299 ? connection.getInputStream() : connection.getErrorStream()
            ));

            StringBuilder responseBody = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBody.append(line.trim());
            }

            // Imprime os detalhes da resposta no console.
            System.out.println("\n=== Resposta do Servidor ===");
            System.out.println("Código de resposta: " + responseCode);
            System.out.println("Corpo da resposta: " + responseBody.toString());

        } catch (IOException e) {
            System.err.println("Erro ao conectar com o servidor: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect(); // Garante que a conexão seja sempre fechada.
            }
        }
    }
}
