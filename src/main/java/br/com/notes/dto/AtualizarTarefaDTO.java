package br.com.notes.dto;

/**
 * DTO para encapsular os dados necessários para a atualização de uma tarefa existente.
 * Usado como corpo (body) da requisição PUT /tarefas/{id}.
 *
 * @param titulo    O novo título da tarefa.
 * @param descricao A nova descrição da tarefa.
 * @param concluida O novo status de conclusão da tarefa.
 */
public record AtualizarTarefaDTO(String titulo, String descricao, boolean concluida) {
}
