package br.com.notes.Utils;

import br.com.notes.dto.CriarTarefaDTO;
import br.com.notes.model.Tarefa;

import java.time.Instant;

public class TarefaFixture {

    public static Tarefa criarTarefaFixture(int id, String titulo, String descricao, boolean concluida) {
        Tarefa tarefa = new Tarefa();
        tarefa.setId(id);
        tarefa.setTitulo(titulo);
        tarefa.setDescricao(descricao);
        tarefa.setConcluida(concluida);
        tarefa.setDataCriacao(Instant.now());
        return tarefa;
    }

    public static CriarTarefaDTO criarTarefaDTO() {
        CriarTarefaDTO dto = new CriarTarefaDTO("Teste", "Testando");
        return dto;
    }
}
