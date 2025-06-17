package br.com.notes;

import br.com.notes.config.DbConfig;
import br.com.notes.controller.TarefaController;
import br.com.notes.controller.UtilController;
import br.com.notes.repository.TarefaRepository;
import br.com.notes.service.TarefaService;
import io.javalin.Javalin;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.json.JavalinJackson;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Set;


public class App {

    private static final String TOKEN_AUTENTICACAO = "vasco-da-gama";
    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        var tarefaService = setupDependencies();
        Javalin app = createAndConfigureApp(tarefaService);
        printStartupBanner(app);
    }

    /**
     * Centraliza a criação e configuração da instância do Javalin.
     */
    private static Javalin createAndConfigureApp(TarefaService tarefaService) {
        Javalin app = Javalin.create(config -> {
            // Configurações globais do Javalin podem vir aqui
            config.jsonMapper(new JavalinJackson());
            config.showJavalinBanner = false; // Desativa o banner padrão do Javalin
        });

        // Registra os middlewares (validação de acesso, logs, etc.)
        registerMiddlewares(app);

        // Registra todas as rotas da aplicação
        registerRoutes(app, tarefaService);

        // Inicia o servidor na porta 7000
        return app.start(7000);
    }

    /**
     * Configura e registra os middlewares da aplicação.
     */
    private static void registerMiddlewares(Javalin app) {
        // Middleware para logar todas as requisições após serem processadas
        app.after(ctx -> {
            log.info("Requisição {} {} -> status {}", ctx.method(), ctx.path(), ctx.status());
        });

        // Middleware de validação de acesso (executa antes de cada requisição)
        app.before(ctx -> {
            // Define um conjunto de rotas que não precisam de autenticação
            Set<String> rotasPublicas = Set.of("/hello", "/status", "/echo", "/saudacao");

            // Verifica se o início da rota atual está na lista de rotas públicas
            boolean ehRotaPublica = rotasPublicas.stream().anyMatch(rota -> ctx.path().startsWith(rota));

            if (ehRotaPublica) {
                return; // Se for pública, permite o acesso
            }

            // Para todas as outras rotas, valida o token
            String tokenRecebido = ctx.header("Authorization");
            if (!TOKEN_AUTENTICACAO.equals(tokenRecebido)) {
                throw new UnauthorizedResponse("Token inválido ou ausente!");
            }
        });
    }

    /**
     * Centraliza o registro de todos os controllers e suas rotas.
     */
    private static void registerRoutes(Javalin app, TarefaService tarefaService) {
        new UtilController().registrarRotas(app);
        new TarefaController(tarefaService).registrarRotas(app);
    }

    /**
     * Centraliza a criação das dependências (Injeção de Dependência manual).
     */
    @NotNull
    private static TarefaService setupDependencies() {
        var jdbi = DbConfig.createJdbi();
        var tarefaRepository = new TarefaRepository(jdbi);
        return new TarefaService(tarefaRepository);
    }

    /**
     * Imprime um banner informativo no console ao iniciar a aplicação.
     */
    private static void printStartupBanner(Javalin app) {
        log.info("\n" +
                "==================================================================\n" +
                "  API de Gerenciamento de Tarefas iniciada com sucesso!  \n" +
                "  Servidor rodando em: http://localhost:" + app.port() + "\n" +
                "  Use o token de autenticação: " + TOKEN_AUTENTICACAO + "\n" +
                "==================================================================");
    }

        /*
        Javalin app = Javalin.create().start(7000);
        var tarefaService = instanciarTodasAsClasses();

        validacaoDeAcesso(app);
        // Rota GET
        app.get("/hello", ctx -> {
            ctx.result("Hello, Javalin!");
        });

        // Rota GET
        app.get("/status", ctx -> {
            Map<String, String> statusInfo = Map.of(
                    "status", "ok",
                    "timestamp", OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) // Pega a hora atual e formata para ISO-8601
            );
            // Usa ctx.json() para enviar o Map como uma resposta JSON.
            ctx.json(statusInfo);
        });

        app.post("/echo", ctx -> {

            Mensagem mensagemRecebida = ctx.bodyAsClass(Mensagem.class);

            ctx.json(mensagemRecebida);
        });

        app.get("/saudacao/{nome}", ctx -> {
            // Captura o valor do parâmetro "nome" da URL.
            String nome = ctx.pathParam("nome");

            // Cria a mensagem de saudação.
            String textoSaudacao = "Olá, " + nome + "!";

            // Cria um objeto Mensagem para a resposta JSON.
            Mensagem mensagemDeSaudacao = new Mensagem(textoSaudacao);

            // Retorna o objeto como JSON.
            ctx.json(mensagemDeSaudacao);
        });

        app.after(ctx -> {
            log.info("Requisição {} {} -> status {}", ctx.method(), ctx.path(), ctx.status());
        });

        new TarefaController(tarefaService).registrarRotas(app);
    }
    public record Mensagem(String mensagem) {}

    private static void validacaoDeAcesso(Javalin app) {
        app.before(ctx -> {
            String rota = ctx.path();
            if (rota.startsWith("/teste")) {
                return;
            }
            if (rota.startsWith("/hello")) {
                return;
            }

            if (rota.startsWith("/status")) {
                return;
            }

            if (rota.startsWith("/echo")) {
                return;
            }

            if (rota.startsWith("/saudacao/{nome}")) {
                return;
            }

            String tokenRecebido = ctx.header("Authorization");

            if (tokenRecebido == null || !tokenRecebido.equals(TOKEN_AUTENTICACAO)) {
                throw new UnauthorizedResponse("Token inválido ou ausente!");
            }
        });
    }

    @NotNull
    private static TarefaService instanciarTodasAsClasses() {
        var dbConfig = DbConfig.createJdbi();
        var tarefaRepository = new TarefaRepository(dbConfig);
        return new TarefaService(tarefaRepository);
    }

      */

}