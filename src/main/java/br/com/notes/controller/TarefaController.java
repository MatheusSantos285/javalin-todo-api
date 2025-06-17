package br.com.notes.controller;

import br.com.notes.dto.AtualizarTarefaDTO;
import br.com.notes.dto.CriarTarefaDTO;
import br.com.notes.service.TarefaService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;

/**
 * Controller responsável por gerenciar as requisições HTTP para o recurso 'tarefas'.
 * Ele faz a ponte entre as requisições da web e a lógica de negócio na TarefaService.
 */
public class TarefaController {
    // Constantes para os caminhos das rotas, evitando "magic strings".
    public static final String TAREFA_PATH = "/tarefas";
    public static final String TAREFA_ID_PATH = "/tarefas/{id}";

    private final TarefaService tarefaService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TarefaController(TarefaService tarefaService) {
        this.tarefaService = tarefaService;
    }

    /**
     * Registra todas as rotas de tarefas na instância do Javalin.
     * @param app A instância do Javalin.
     */
    public void registrarRotas(Javalin app) {
        app.get(TAREFA_PATH, this::listarTarefas);
        app.get(TAREFA_ID_PATH, this::buscarPorId);
        app.post(TAREFA_PATH, this::criar);
        app.put(TAREFA_ID_PATH, this::atualizar);
        app.delete(TAREFA_ID_PATH, this::deletar);
    }

    /**
     * Handler para a rota GET /tarefas.
     * Lista todas as tarefas existentes.
     * Retorna status 200 OK com um array de tarefas no corpo da resposta.
     * @param ctx O contexto da requisição do Javalin.
     */
    private void listarTarefas(Context ctx) {
        ctx.json(tarefaService.listarTarefas());
    }

    /**
     * Handler para a rota GET /tarefas/{id}.
     * Busca uma única tarefa pelo seu ID.
     * Retorna status 200 OK com o objeto da tarefa se encontrada.
     * Retorna status 404 Not Found se a tarefa não existir.
     * @param ctx O contexto da requisição do Javalin.
     */
    private void buscarPorId(Context ctx) {
        int id = parseIdParam(ctx);
        ctx.json(tarefaService.buscarPorId(id));
    }

    /**
     * Handler para a rota POST /tarefas.
     * Cria uma nova tarefa com base no corpo da requisição JSON.
     * Retorna status 201 Created com a tarefa recém-criada no corpo da resposta.
     * Retorna status 400 Bad Request se o JSON for inválido ou se o título estiver faltando.
     * @param ctx O contexto da requisição do Javalin.
     */
    private void criar(Context ctx) {
        try {
            String jsonBody = ctx.body();
            System.out.println("JSON recebido: " + jsonBody);

            CriarTarefaDTO dto = objectMapper.readValue(jsonBody, CriarTarefaDTO.class);
            System.out.println("DTO desserializado: " + dto);

            if (dto.titulo() == null || dto.titulo().isBlank()) {
                throw new BadRequestResponse("O campo 'titulo' é obrigatório.");
            }

            var novaTarefa = tarefaService.criar(dto);
            ctx.status(201).json(novaTarefa);

        } catch (JsonProcessingException e) {
            System.err.println("Erro de desserialização: " + e.getMessage()); // Log detalhado
            throw new BadRequestResponse("Corpo da requisição inválido. Certifique-se de enviar um JSON válido.");
        }
    }

    /**
     * Handler para a rota PUT /tarefas/{id}.
     * Atualiza uma tarefa existente com base nos dados do corpo da requisição.
     * Retorna status 200 OK com a tarefa atualizada no corpo da resposta.
     * Retorna status 404 Not Found se a tarefa não existir.
     * @param ctx O contexto da requisição do Javalin.
     */
    private void atualizar(Context ctx) {
        try {
            int id = parseIdParam(ctx);
            String jsonBody = ctx.body();
            AtualizarTarefaDTO dto = objectMapper.readValue(jsonBody, AtualizarTarefaDTO.class);

            if (dto.titulo() == null || dto.titulo().isBlank()) {
                throw new BadRequestResponse("O campo 'titulo' é obrigatório.");
            }

            var tarefaAtualizada = tarefaService.atualizar(id, dto);
            ctx.status(200).json(tarefaAtualizada);
        } catch (JsonProcessingException e) {
            throw new BadRequestResponse("Corpo da requisição inválido. Certifique-se de enviar um JSON válido.");
        } catch (BadRequestResponse e) {
            ctx.status(400).result(e.getMessage());
        } catch (Exception e) {
            ctx.status(404).result("Tarefa não encontrada com o ID fornecido.");
        }
    }

    /**
     * Handler para a rota DELETE /tarefas/{id}.
     * Deleta uma tarefa pelo seu ID.
     * Retorna status 204 No Content em caso de sucesso.
     * Retorna status 404 Not Found se a tarefa não existir.
     * @param ctx O contexto da requisição do Javalin.
     */
    private void deletar(Context ctx) {
        int id = parseIdParam(ctx);
        tarefaService.deletar(id);
        ctx.status(204); // Status 204 No Content, indicando sucesso sem corpo de resposta.
    }

    /**
     * Método auxiliar para converter o path param 'id' para um inteiro.
     * Lança uma BadRequestResponse se o ID for inválido.
     * @param ctx O contexto da requisição.
     * @return O ID como um inteiro.
     */
    private int parseIdParam(Context ctx) {
        try {
            return Integer.parseInt(ctx.pathParam("id"));
        } catch (NumberFormatException e) {
            throw new BadRequestResponse("ID inválido. Use um numero inteiro!");
        }
    }
}
