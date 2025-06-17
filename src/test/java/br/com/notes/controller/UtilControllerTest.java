package br.com.notes.controller;

import br.com.notes.config.DbConfig;
import br.com.notes.dto.CriarTarefaDTO;
import br.com.notes.repository.TarefaRepository;
import br.com.notes.service.TarefaService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import okhttp3.Response;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;


/**
 * Classe de testes de integração para a API de Tarefas.
 * O objetivo destes testes é simular requisições HTTP reais para os endpoints
 * e verificar se a aplicação como um todo (Controller, Service, Repository e Banco de Dados)
 * se comporta como o esperado.
 */
class UtilControllerTest {

    private Javalin app;
    private Jdbi jdbi;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Este método é executado antes de cada teste.
     * Ele é responsável por montar um ambiente limpo para cada teste,
     * configurando uma nova instância da aplicação e do banco de dados em memória.
     */
    @BeforeEach
    void setUp() {
        app = Javalin.create();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        jdbi = DbConfig.createJdbi();
        var tarefaRepository = new TarefaRepository(jdbi);
        var tarefaService = new TarefaService(tarefaRepository);

        new TarefaController(tarefaService).registrarRotas(app);
        new UtilController().registrarRotas(app);
    }

    /**
     * Testa o endpoint GET /hello.
     * Propósito: Verificar se a aplicação está rodando e respondendo a requisições básicas.
     * É um "smoke test" que confirma que o servidor subiu corretamente.
     */
    @Test
    void testGET_hello_retorna_200_e_mensagem_correta() {
        JavalinTest.test(app, (server, client) -> {
            Response response = client.get("/hello");

            assertEquals(200, response.code());
            assertThat(response.body().string()).isEqualTo("Hello, Javalin!");
        });
    }

    /**
     * Testa o endpoint POST /tarefas.
     * Propósito: Validar a funcionalidade de criação de uma nova tarefa.
     * Verifica se o servidor aceita o JSON, cria o recurso e retorna o status 201 (Created).
     */
    @Test
    void testPOST_tarefas_cria_recurso_e_retorna_201() {
        JavalinTest.test(app, (server, client) -> {
            CriarTarefaDTO novaTarefaDto = new CriarTarefaDTO("Tarefa criada no teste", "Descrição via teste");

            String jsonBody = objectMapper.writeValueAsString(novaTarefaDto);
            System.out.println("Enviando JSON: " + jsonBody); // Log do JSON enviado

            Response response = client.post(
                    "/tarefas",
                    jsonBody,
                    request -> {
                        request.header("Content-Type", "application/json");
                        request.header("Accept", "application/json");
                    }
            );

            System.out.println("Status code: " + response.code());
            String responseBody = response.body().string();
            System.out.println("Response body: " + responseBody);

            assertEquals(201, response.code());
            assertThat(responseBody).contains("Tarefa criada no teste");
        });
    }

    /**
     * Testa o endpoint GET /tarefas/{id}.
     * Propósito: Validar a busca de uma tarefa específica pelo seu ID.
     */
    @Test
    void testGET_tarefa_por_id_retorna_tarefa_especifica_e_status_200() {
        JavalinTest.test(app, (server, client) -> {
            // Primeiro, cria uma tarefa para garantir que haja algo para buscar
            CriarTarefaDTO novaTarefaDto = new CriarTarefaDTO("Tarefa para teste", "Descrição via teste");
            String jsonBody = objectMapper.writeValueAsString(novaTarefaDto);

            Response postResponse = client.post(
                    "/tarefas",
                    jsonBody,
                    request -> {
                        request.header("Content-Type", "application/json");
                        request.header("Accept", "application/json");
                    }
            );

            assertEquals(201, postResponse.code());
            String responseBody = postResponse.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            int tarefaId = jsonNode.get("id").asInt();

            // Agora busca a tarefa pelo ID
            Response getResponse = client.get("/tarefas/" + tarefaId);
            assertEquals(200, getResponse.code());

            String getResponseBody = getResponse.body().string();
            System.out.println("Response body: " + responseBody);
            assertThat(getResponseBody).contains("Tarefa para teste");
        });
    }

    /**
     * Testa o endpoint GET /tarefas.
     * Propósito: Validar a listagem de todas as tarefas existentes.
     */
    @Test
    void testGET_tarefas_retorna_lista_com_tarefa_criada() {
        JavalinTest.test(app, (server, client) -> {
            CriarTarefaDTO novaTarefaDto = new CriarTarefaDTO("Tarefa para teste", "Descrição via teste");
            String jsonBody = objectMapper.writeValueAsString(novaTarefaDto);

            Response postResponse = client.post(
                    "/tarefas",
                    jsonBody,
                    request -> {
                        request.header("Content-Type", "application/json");
                        request.header("Accept", "application/json");
                    }
            );

            assertEquals(201, postResponse.code());

            Response getResponse = client.get("/tarefas");
            assertEquals(200, getResponse.code());

            String responseBody = getResponse.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            assertTrue(jsonNode.isArray(), "A resposta deve ser um array JSON");
            assertTrue(jsonNode.size() > 0, "O array de tarefas não deve estar vazio");

            System.out.println("Response body: " + responseBody);
            boolean tarefaEncontrada = false;
            for (JsonNode tarefa : jsonNode) {
                if (tarefa.get("titulo").asText().equals("Tarefa para teste")) {
                    tarefaEncontrada = true;
                    break;
                }
            }
            assertTrue(tarefaEncontrada, "A tarefa criada deve aparecer na listagem");
        });
    }
}