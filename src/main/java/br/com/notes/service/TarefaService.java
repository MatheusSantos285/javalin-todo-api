package br.com.notes.service;

import br.com.notes.dto.AtualizarTarefaDTO;
import br.com.notes.dto.CriarTarefaDTO;
import br.com.notes.dto.RespostaTarefaDTO;
import br.com.notes.model.Tarefa;
import br.com.notes.repository.TarefaRepository;
import io.javalin.http.NotFoundResponse;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class TarefaService {
    private final TarefaRepository tarefaRepository;

    public TarefaService(TarefaRepository tarefaRepository) {
        this.tarefaRepository = tarefaRepository;
    }

    /**
     * Retorna uma lista de todas as tarefas, já convertidas para o DTO de resposta.
     */
    public List<RespostaTarefaDTO> listarTarefas() {
        return tarefaRepository.findAll().stream()
                .map(this::converterParaRespostaTarefaDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca uma tarefa por ID e a retorna como um DTO de resposta.
     * Lança NotFoundResponse se não encontrar.
     */
    public RespostaTarefaDTO buscarPorId(int id) {
        Tarefa tarefa = tarefaRepository.findById(id)
                .orElseThrow(() -> new NotFoundResponse("Tarefa não encontrada com o ID: " + id));
        return converterParaRespostaTarefaDTO(tarefa);
    }

    /**
     * Cria uma nova tarefa a partir de um DTO e retorna a tarefa criada como um DTO de resposta.
     */
    public RespostaTarefaDTO criar(CriarTarefaDTO dto) {
        Tarefa novaTarefa = new Tarefa();
        novaTarefa.setTitulo(dto.titulo());
        novaTarefa.setDescricao(dto.descricao());
        var novoId = tarefaRepository.insert(novaTarefa);
        return buscarPorId(novoId);
    }

    /**
     * Atualiza uma tarefa existente com os dados do DTO e retorna a tarefa atualizada como um DTO de resposta.
     * Lança NotFoundResponse se a tarefa não for encontrada.
     */
    public RespostaTarefaDTO atualizar(int id, AtualizarTarefaDTO dto) {
        Tarefa tarefaExistente = tarefaRepository.findById(id)
                .orElseThrow(() -> new NotFoundResponse("Tarefa não encontrada com o ID: " + id));

        tarefaExistente.setTitulo(dto.titulo());
        tarefaExistente.setDescricao(dto.descricao());
        tarefaExistente.setConcluida(dto.concluida());
        tarefaRepository.update(tarefaExistente);

        return converterParaRespostaTarefaDTO(tarefaExistente);
    }

    /**
     * Deleta uma tarefa pelo ID.
     * Lança NotFoundResponse se a tarefa não for encontrada.
     */
    public void deletar(int id) {
        tarefaRepository.findById(id)
                .orElseThrow(() -> new NotFoundResponse("Tarefa não encontrada com o ID: " + id));

        tarefaRepository.delete(id);
    }

    /**
     * Método auxiliar privado para converter o modelo Tarefa para o DTO de Resposta.
     */
    private RespostaTarefaDTO converterParaRespostaTarefaDTO(Tarefa tarefa) {
        return new RespostaTarefaDTO(
                tarefa.getId(),
                tarefa.getTitulo(),
                tarefa.getDescricao(),
                tarefa.isConcluida(),
                tarefa.getDataCriacao()
        );
    }
}
