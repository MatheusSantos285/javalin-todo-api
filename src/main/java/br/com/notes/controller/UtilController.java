package br.com.notes.controller;

import io.javalin.Javalin;
import io.javalin.http.Context;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Controller para as rotas de utilidade e teste inicial do projeto.
 * Estes endpoints servem para verificar a saúde da API e para fins de demonstração.
 */
public class UtilController {

    /**
     * Registra todas as rotas de utilidade na instância do Javalin.
     * @param app A instância do Javalin.
     */
    public void registrarRotas(Javalin app) {
        app.get("/hello", this::hello);
        app.get("/status", this::status);
        app.post("/echo", this::echo);
        app.get("/saudacao/{nome}", this::saudacao);
    }

    /**
     * Handler para a rota GET /hello.
     * Retorna uma saudação simples para confirmar que a API está online.
     * @param ctx O contexto da requisição do Javalin.
     */
    private void hello(Context ctx) {
        ctx.result("Hello, Javalin!");
    }

    /**
     * Handler para a rota GET /status.
     * Retorna um objeto JSON com o status "ok" e o timestamp atual em ISO-8601.
     * @param ctx O contexto da requisição do Javalin.
     */
    private void status(Context ctx) {
        Map<String, String> statusInfo = Map.of(
                "status", "ok",
                "timestamp", OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        );
        ctx.json(statusInfo);
    }

    /**
     * Handler para a rota POST /echo.
     * Recebe um JSON com a chave "mensagem" e retorna o mesmo JSON como resposta.
     * @param ctx O contexto da requisição do Javalin.
     */
    private void echo(Context ctx) {
        Mensagem mensagem = ctx.bodyAsClass(Mensagem.class);
        ctx.json(mensagem);
    }

    /**
     * Handler para a rota GET /saudacao/{nome}.
     * Recebe um nome como parâmetro na URL e retorna uma saudação personalizada em JSON.
     * @param ctx O contexto da requisição do Javalin.
     */
    private void saudacao(Context ctx) {
        String nome = ctx.pathParam("nome");
        ctx.json(new Mensagem("Olá, " + nome + "!"));
    }

    /**
     * Record interno para representar o corpo JSON das rotas /echo e /saudacao.
     * @param mensagem O conteúdo da mensagem.
     */
    private record Mensagem(String mensagem) {}
}
