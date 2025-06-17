package br.com.notes.dto;

/**
 * DTO para encapsular os dados necessários para a criação de uma nova tarefa.
 * Usado como corpo (body) da requisição POST /tarefas.
 *
 * @param titulo    O título obrigatório da nova tarefa.
 * @param descricao A descrição opcional da nova tarefa.
 */
public record CriarTarefaDTO(String titulo, String descricao) {
}
