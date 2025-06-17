package br.com.notes.dto;

import java.time.Instant;

/**
 * DTO (Data Transfer Object) para representar a resposta de uma tarefa.
 * Este é o formato de dados que a API retorna ao cliente ao listar ou buscar tarefas.
 *
 * @param id          O identificador único da tarefa.
 * @param titulo      O título da tarefa.
 * @param descricao   A descrição detalhada da tarefa (pode ser nula).
 * @param concluida   Indica se a tarefa foi marcada como concluída.
 * @param dataCriacao A data e hora exatas (em UTC) em que a tarefa foi criada.
 */
public record RespostaTarefaDTO(int id, String titulo, String descricao, boolean concluida, Instant dataCriacao) {
}
