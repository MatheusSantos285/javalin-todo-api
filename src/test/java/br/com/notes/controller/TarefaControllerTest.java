package br.com.notes.controller;

import br.com.notes.dto.AtualizarTarefaDTO;
import br.com.notes.dto.CriarTarefaDTO;
import br.com.notes.dto.RespostaTarefaDTO;
import br.com.notes.model.Tarefa;
import br.com.notes.service.TarefaService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.http.NotFoundResponse;
import io.javalin.json.JavalinJackson;
import io.javalin.testtools.JavalinTest;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import static br.com.notes.Utils.TarefaFixture.criarTarefaDTO;
import static br.com.notes.Utils.TarefaFixture.criarTarefaFixture;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Testes de unidade para o TarefaController.
 * <p>
 * O objetivo é testar como o controller reage às requisições HTTP,
 * validando o status da resposta e o corpo retornado. A camada de serviço
 * (TarefaService) é mockada para isolar o controller e testar apenas
 * sua responsabilidade de lidar com HTTP.
 */
@ExtendWith(MockitoExtension.class)
class TarefaControllerTest {

    @Mock
    private TarefaService tarefaService;

    @InjectMocks
    private TarefaController tarefaController;

    private ObjectMapper objectMapper;
    private Javalin app;

    private Tarefa tarefaExemplo;

    /**
     * Prepara o ambiente antes de cada teste.
     * <p>
     * Este método cria uma nova instância do Javalin e do ObjectMapper,
     * garantindo que cada teste execute em um ambiente limpo e isolado.
     * Também registra as rotas do controller na instância do Javalin.
     */
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        app = Javalin.create(config -> config.jsonMapper(new JavalinJackson()));
        tarefaController.registrarRotas(app);

        tarefaExemplo = criarTarefaFixture(1, "Tarefa Padrão", "Descrição Padrão", false);
    }

    /**
     * Testa: GET /tarefas
     * Cenário: Existem tarefas a serem listadas.
     * Verifica se: O endpoint retorna status 200 OK e uma lista de tarefas em JSON.
     */
    @Test
    @DisplayName("Deve listar todas as tarefas com sucesso")
    void deveListarTodasAsTarefas() {
        // Arrange
        List<RespostaTarefaDTO> listaDeDtos = List.of(
                new RespostaTarefaDTO(1, "Tarefa Teste 1", "Desc 1", false, Instant.now())
        );
        when(tarefaService.listarTarefas()).thenReturn(listaDeDtos);

        // Act & Assert
        JavalinTest.test(criarAppComRotas(), (server, client) -> {
            var response = client.get("/tarefas");

            assertEquals(200, response.code());
            assertNotNull(response.body());

            List<RespostaTarefaDTO> responseTarefas = objectMapper.readValue(
                    response.body().string(),
                    new TypeReference<List<RespostaTarefaDTO>>() {
                    }
            );

            assertEquals(1, responseTarefas.size());
            assertEquals("Tarefa Teste 1", responseTarefas.get(0).titulo());
        });

        verify(tarefaService).listarTarefas();
    }

    /**
     * Testa: GET /tarefas
     * Cenário: Não existem tarefas cadastradas.
     * Verifica se: O endpoint retorna status 200 OK e um array JSON vazio.
     */
    @Test
    @DisplayName("Deve retornar um array vazio quando não houver tarefas")
    void deveRetornarArrayVazioQuandoNaoHouverTarefas() {
        // Arrange
        when(tarefaService.listarTarefas()).thenReturn(List.of());

        // Act & Assert
        JavalinTest.test(criarAppComRotas(), (server, client) -> {
            var response = client.get("/tarefas");

            assertEquals(200, response.code());
            assertNotNull(response.body());

            List<RespostaTarefaDTO> responseTarefas = objectMapper.readValue(
                    response.body().string(),
                    new TypeReference<List<RespostaTarefaDTO>>() {
                    }
            );

            assertTrue(responseTarefas.isEmpty());
        });
    }

    /**
     * Testa: GET /tarefas/{id}
     * Cenário: A tarefa com o ID solicitado existe.
     * Verifica se: O endpoint retorna status 200 OK e os dados da tarefa correta.
     */
    @Test
    @DisplayName("Deve listar todas as tarefas com sucesso")
    void deveBuscarTarefaPorId() {
        // Arrange
        int idDaTarefa = 5;

        RespostaTarefaDTO dtoEsperado = new RespostaTarefaDTO(
                idDaTarefa, "Tarefa Encontrada", "Descrição da tarefa encontrada", false, Instant.now()
        );

        when(tarefaService.buscarPorId(idDaTarefa)).thenReturn(dtoEsperado);

        // Act & Assert
        JavalinTest.test(criarAppComRotas(), (server, client) -> {
            var response = client.get("/tarefas/" + idDaTarefa);
            assertEquals(200, response.code());
            assert response.body() != null;

            RespostaTarefaDTO responseTarefa = objectMapper.readValue(
                    response.body().string(),
                    RespostaTarefaDTO.class
            );

            assertEquals(dtoEsperado.id(), responseTarefa.id());
            assertEquals(dtoEsperado.titulo(), responseTarefa.titulo());
            assertEquals(dtoEsperado.descricao(), responseTarefa.descricao());
        });
    }

    /**
     * Testa: GET /tarefas/{id}
     * Cenário: A tarefa com o ID solicitado não existe.
     * Verifica se: O endpoint retorna status 404 Not Found.
     */
    @Test
    @DisplayName("Deve retornar 404 ao buscar tarefa com ID inexistente")
    void deveRetornar404AoBuscarTarefaInexistente() {
        // Arrange
        int idInexistente = 5;
        when(tarefaService.buscarPorId(idInexistente)).thenThrow(new NotFoundResponse("Tarefa não encontrada com o ID: " + idInexistente));

        // Act & Assert
        JavalinTest.test(criarAppComRotas(), (server, client) -> {
            var response = client.get("/tarefas/" + idInexistente);
            assertEquals(404, response.code());
        });

        verify(tarefaService).buscarPorId(anyInt());
    }

    /**
     * Testa: GET /tarefas/{id}
     * Cenário: O ID fornecido na URL não é um número.
     * Verifica se: O endpoint retorna status 400 Bad Request e a service não é chamada.
     */
    @Test
    @DisplayName("Deve retornar 400 ao buscar tarefa com ID inválido")
    void deveRetornar400ComIdInvalido() {
        JavalinTest.test(criarAppComRotas(), (server, client) -> {
            var response = client.get("/tarefas/hdhdh");
            assertEquals(400, response.code());
            assert response.body() != null;
            assertTrue(response.body().string().contains("ID inválido. Use um numero inteiro!"));
        });

        verifyNoInteractions(tarefaService);
    }

    /**
     * Testa: POST /tarefas
     * Cenário: Os dados enviados para criação são válidos.
     * Verifica se: O endpoint retorna status 201 Created e os dados da tarefa criada.
     */
    @Test
    @DisplayName("Deve criar uma nova tarefa com sucesso")
    void deveCriarNovaTarefa() {
        // Arrange
        CriarTarefaDTO dto = criarTarefaDTO();
        int idDaTarefa = 5;
        RespostaTarefaDTO dtoEsperado = new RespostaTarefaDTO(
                idDaTarefa, dto.titulo(), dto.descricao(), false, Instant.now()
        );
        when(tarefaService.criar(any(CriarTarefaDTO.class))).thenReturn(dtoEsperado);

        // Act & Assert
        JavalinTest.test(criarAppComRotas(), (server, client) -> {
            String jsonBody = objectMapper.writeValueAsString(dto);

            var response = client.post("/tarefas", jsonBody, request -> {
                request.header("Content-Type", "application/json");
                request.header("Accept", "application/json");
            });

            assertEquals(201, response.code());

            var responseTarefa = objectMapper.readValue(
                    response.body().string(),
                    RespostaTarefaDTO.class
            );

            assertEquals(dto.titulo(), responseTarefa.titulo());
            assertEquals(dto.descricao(), responseTarefa.descricao());
        });
    }

    /**
     * Fornece dados inválidos para o teste de criação de tarefa.
     */
    static Stream<Arguments> dadosInvalidosParaCriacaoDeTarefas() {
        return Stream.of(
                Arguments.of("", "Descrição 1", "O campo 'titulo' é obrigatório."),
                Arguments.of(null, "Descrição 1", "O campo 'titulo' é obrigatório.")
        );
    }
    /**
     * Testa: POST /tarefas
     * Cenário: Os dados enviados para criação são inválidos (título nulo ou vazio).
     * Verifica se: O endpoint retorna status 400 Bad Request e a service não é chamada.
     */
    @ParameterizedTest
    @MethodSource("dadosInvalidosParaCriacaoDeTarefas")
    @DisplayName("Deve retornar 400 ao tentar criar tarefa com título inválido")
    void deveRetornar400AoCriarTarefacomTituloVazio(String titulo, String descricao, String mensagemErro) {
        // Arrange
        CriarTarefaDTO dto = new CriarTarefaDTO(titulo, descricao);

        // Act & Assert
        JavalinTest.test(criarAppComRotas(), (server, client) -> {
            String jsonBody = objectMapper.writeValueAsString(dto);
            var response = client.post("/tarefas", jsonBody, request -> {
                request.header("Content-Type", "application/json");
                request.header("Accept", "application/json");
            });

            assertEquals(400, response.code());
            assert response.body() != null;
            assertTrue(response.body().string().contains(mensagemErro));
        });

        verifyNoInteractions(tarefaService);
    }

    /**
     * Testa: PUT /tarefas/{id}
     * Cenário: Os dados enviados para atualização são válidos e a tarefa existe.
     * Verifica se: O endpoint retorna status 200 OK e os dados da tarefa atualizada.
     */
    @Test
    @DisplayName("Deve atualizar uma tarefa com sucesso")
    void deveAtualizarTarefa() {
        // Arrange
        AtualizarTarefaDTO dto = new AtualizarTarefaDTO("Tarefa Padrão", "Descrição Padrão", true);
        int idDaTarefa = 5;
        RespostaTarefaDTO dtoAtualizado = new RespostaTarefaDTO(
                idDaTarefa, dto.titulo(), dto.descricao(), false, Instant.now()
        );
        when(tarefaService.atualizar(idDaTarefa, dto)).thenReturn(dtoAtualizado);

        // Act & Assert
        JavalinTest.test(criarAppComRotas(), (server, client) -> {
            String jsonBody = objectMapper.writeValueAsString(dto);
            var response = client.put("/tarefas/" + idDaTarefa, jsonBody, request -> {
                request.header("Content-Type", "application/json");
                request.header("Accept", "application/json");
            });

            assertEquals(200, response.code());

            var responseBody = objectMapper.readValue(
                    response.body().string(),
                    RespostaTarefaDTO.class
            );

            assertEquals(dto.titulo(), responseBody.titulo());
        });
    }

    /**
     * Testa: DELETE /tarefas/{id}
     * Cenário: A tarefa com o ID solicitado não existe.
     * Verifica se: O endpoint retorna status 404 Not Found.
     */
    @Test
    @DisplayName("Deve retornar 404 ao tentar deletar tarefa inexistente")
    void deveDeletarTarefa() {
        int idDaTarefa = 5;

        doNothing().when(tarefaService).deletar(idDaTarefa);

        JavalinTest.test(criarAppComRotas(), (server, client) -> {
            var response = client.delete("/tarefas/"+ idDaTarefa);
            assertEquals(204, response.code());

            assert response.body() != null;
            assertEquals("", response.body().string());
        });

        verify(tarefaService).deletar(idDaTarefa);
    }

    /**
     * Testa: DELETE /tarefas/{id}
     * Cenário: A tarefa com o ID solicitado não existe.
     * Verifica se: O endpoint retorna status 404 Not Found.
     */
    @Test
    @DisplayName("Deve retornar 404 ao tentar deletar tarefa inexistente")
    void deveRetornar404AoDeletarTarefaInexistente() {
        // Arrange
        int idDaTarefa = 5;
        doThrow(new NotFoundResponse()).when(tarefaService).deletar(idDaTarefa);

        // Act & Assert
        JavalinTest.test(criarAppComRotas(), (server, client) -> {
            var response = client.delete("/tarefas/"+ idDaTarefa);
            assertEquals(404, response.code());
        });

        verify(tarefaService).deletar(idDaTarefa);
    }

    @NotNull
    private Javalin criarAppComRotas() {
        Javalin app = Javalin.create();
        tarefaController.registrarRotas(app);
        return app;
    }
}