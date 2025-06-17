package br.com.notes.repository;

import br.com.notes.model.Tarefa;
import org.jdbi.v3.core.Jdbi;

import java.util.List;
import java.util.Optional;

public class TarefaRepository {
    private final Jdbi dataSource;

    public TarefaRepository(Jdbi dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Método para buscar todas as tarefas.
     *
     * @return Lista de tarefas.
     */
    public List<Tarefa> findAll() {
        return dataSource.withHandle(handle ->
            handle.createQuery("SELECT * FROM tarefas")
                  .mapToBean(Tarefa.class)
                  .list()
        );
    }

    /**
     * Método para buscar uma tarefa pelo ID.
     *
     * @param id ID da tarefa a ser buscada.
     * @return Optional contendo a tarefa, se encontrada.
     */
    public Optional<Tarefa> findById(int id) {
        return dataSource.withHandle(handle ->
            handle.createQuery("SELECT * FROM tarefas WHERE id = :id")
                  .bind("id", id)
                  .mapToBean(Tarefa.class)
                  .findFirst()
        );
    }

    /**
     * Método para inserir uma nova tarefa.
     *
     * @param tarefa Tarefa a ser inserida.
     * @return ID da tarefa inserida.
     */
    public int insert(Tarefa tarefa) {
        return dataSource.withHandle(handle ->
                handle.createUpdate("INSERT INTO tarefas (titulo, descricao) " +
                                "VALUES (:titulo, :descricao)")
                        .bind("titulo", tarefa.getTitulo())
                        .bind("descricao", tarefa.getDescricao())
                        .executeAndReturnGeneratedKeys()
                        .map((rs, ctx) -> rs.getInt((1)))
                        .one());

    }

    /**
     * Método para atualizar uma tarefa existente.
     *
     * @param tarefa Tarefa com os dados atualizados.
     */
    public void update(Tarefa tarefa) {
        dataSource.withHandle(handle ->
            handle.createUpdate("UPDATE tarefas SET titulo = :titulo, descricao = :descricao, " +
                                "concluida = :concluida WHERE id = :id")
                    .bindBean(tarefa)
                    .execute()
        );
    }

    /**
     * Método para deletar uma tarefa pelo ID.
     *
     * @param id ID da tarefa a ser deletada.
     */
    public void delete(int id) {
        dataSource.withHandle(handle ->
            handle.createUpdate("DELETE FROM tarefas WHERE id = :id")
                    .bind("id", id)
                    .execute()
        );
    }
}
