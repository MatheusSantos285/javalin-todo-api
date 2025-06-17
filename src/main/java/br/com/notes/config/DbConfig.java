package br.com.notes.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jdbi.v3.core.Jdbi;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;

public class DbConfig {
    /**
     * Cria uma instância do Jdbi configurada para usar um banco de dados H2 em memória.
     * Executa o script SQL fornecido para inicializar o banco de dados.
     *
     * @return uma instância do Jdbi configurada
     */
    public static Jdbi createJdbi() {
        var ds = createDaSouce();
        try (Connection conn = ds.getConnection()) {
            runScript(conn, "script.sql");
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
        return Jdbi.create(ds);
    }

    /**
     * Cria uma instância do DataSource configurada para usar um banco de dados H2 em memória.
     *
     * @return uma instância do DataSource configurada
     */
    private static DataSource createDaSouce() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        return new HikariDataSource(config);
    }

    /**
     * Executa um script SQL a partir de um recurso no classpath.
     *
     * @param conn    a conexão com o banco de dados
     * @param resource o caminho do recurso SQL
     * @throws IOException  se ocorrer um erro ao ler o recurso
     * @throws SQLException se ocorrer um erro ao executar o script SQL
     */
    private static void runScript(Connection conn, String resource) throws IOException, SQLException {
        var input = DbConfig.class.getClassLoader().getResourceAsStream(resource);
        if (input == null) throw new IOException("Arquivo não encontrado: " + resource);
        var sql = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        try (var stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }
}
