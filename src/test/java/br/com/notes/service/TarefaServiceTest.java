package br.com.notes.service;

import br.com.notes.dto.AtualizarTarefaDTO;
import br.com.notes.dto.CriarTarefaDTO;
import br.com.notes.dto.RespostaTarefaDTO;
import br.com.notes.model.Tarefa;
import br.com.notes.repository.TarefaRepository;
import io.javalin.http.NotFoundResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static br.com.notes.Utils.TarefaFixture.criarTarefaFixture;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de unidade para a classe TarefaService.
 * <p>
 * O objetivo é testar a lógica de negócio da camada de serviço de forma isolada,
 * usando um "mock" (versão falsa) do TarefaRepository para simular o acesso
 * ao banco de dados e garantir que a service se comporte como esperado em cada cenário.
 */
@ExtendWith(MockitoExtension.class)
class TarefaServiceTest {

    @Mock
    private TarefaRepository repository; // Mock (dublê) do nosso repositório

    @InjectMocks
    private TarefaService service; // A classe que queremos testar, com o mock injetado nela

    private Tarefa tarefaExemplo;
    private CriarTarefaDTO criarDtoExemplo;
    private AtualizarTarefaDTO atualizarDtoExemplo;

    @BeforeEach
    void setUp() {
        // Inicializa um objeto Tarefa padrão para ser usado nos testes.
        tarefaExemplo = criarTarefaFixture(1, "Tarefa Padrão", "Descrição Padrão", false);

        // Inicializa DTOs padrão.
        criarDtoExemplo = new CriarTarefaDTO("Nova Tarefa", "Nova Descrição");
        atualizarDtoExemplo = new AtualizarTarefaDTO("Tarefa Atualizada", "Descrição Atualizada", true);
    }

    /**
     * Testa o cenário de sucesso da criação de uma nova tarefa.
     * <p>
     * <b>Verifica se:</b>
     * <ul>
     * <li>O método 'insert' do repositório é chamado.</li>
     * <li>O método 'findById' é chamado para retornar a tarefa recém-criada.</li>
     * <li>O DTO de resposta contém os dados corretos.</li>
     * </ul>
     */
    @Test
    void criar_deveRetornarDtoDaTarefaSalva() {

        // Configura o comportamento do mock
        when(repository.insert(any(Tarefa.class))).thenReturn(1);
        when(repository.findById(tarefaExemplo.getId())).thenReturn(Optional.of(tarefaExemplo));

        // Act (Ação)
        RespostaTarefaDTO result = service.criar(criarDtoExemplo);

        // Assert (Verificação)
        assertNotNull(result);
        assertEquals(tarefaExemplo.getId(), result.id());
    }

    /**
     * Testa o cenário de sucesso da atualização de uma tarefa existente.
     * <p>
     * <b>Verifica se:</b>
     * <ul>
     * <li>O repositório é consultado para encontrar a tarefa.</li>
     * <li>O método 'update' do repositório é chamado com os dados corretos.</li>
     * </ul>
     */
    @Test
    void atualizar_deveModificarTarefaExistente() {

        when(repository.findById(1)).thenReturn(Optional.of(tarefaExemplo));

        service.atualizar(1, atualizarDtoExemplo);

        ArgumentCaptor<Tarefa> tarefaCaptor = ArgumentCaptor.forClass(Tarefa.class);
        verify(repository).update(tarefaCaptor.capture());

        Tarefa tarefaAtualizada = tarefaCaptor.getValue();
        assertEquals("Tarefa Atualizada", tarefaAtualizada.getTitulo());
        assertTrue(tarefaAtualizada.isConcluida());
    }

    /**
     * Testa o cenário de falha ao tentar atualizar uma tarefa que não existe.
     * <p>
     * <b>Verifica se:</b>
     * <ul>
     * <li>Uma exceção 'NotFoundResponse' é lançada.</li>
     * <li>A mensagem de erro é a esperada.</li>
     * </ul>
     */
    @Test
    void atualizar_deveLancarExcecaoSeTarefaNaoExiste() {
        int idInexistente = 999;
        when(repository.findById(idInexistente)).thenReturn(Optional.empty());

        assertThrows(NotFoundResponse.class, () ->
                service.atualizar(999, atualizarDtoExemplo));

        var exception = assertThrows(NotFoundResponse.class, () ->
                service.atualizar(idInexistente, atualizarDtoExemplo));

        assertEquals("Tarefa não encontrada com o ID: " + idInexistente, exception.getMessage());
    }

    /**
     * Testa a listagem de todas as tarefas.
     * <p>
     * <b>Verifica se:</b>
     * <ul>
     * <li>O serviço converte corretamente a lista de 'Tarefa' para uma lista de 'RespostaTarefaDTO'.</li>
     * <li>Os dados na lista de DTOs correspondem aos dados originais.</li>
     * </ul>
     */
    @Test
    void listarTarefas_deveRetornarListaDeDtos() {
        // Arrange
        List<Tarefa> tarefasEsperadas = List.of(
                new Tarefa(1, "Title", "Description", false, Instant.now()),
                new Tarefa(2, "Title", "Description", true, Instant.now())
        );
        when(repository.findAll()).thenReturn(tarefasEsperadas);

        //Act
        List<RespostaTarefaDTO> resultado = service.listarTarefas();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());

        RespostaTarefaDTO primeiroDto = resultado.get(0);
        assertEquals(1, primeiroDto.id());
        assertEquals("Title", primeiroDto.titulo());
        assertFalse(primeiroDto.concluida());

        RespostaTarefaDTO segundoDto = resultado.get(1);
        assertEquals(2, segundoDto.id());
        assertEquals("Title", segundoDto.titulo());
        assertTrue(segundoDto.concluida());

        verify(repository, times(1)).findAll();
    }

    /**
     * Testa a busca por uma tarefa com um ID que existe.
     * <p>
     * <b>Verifica se:</b>
     * <ul>
     * <li>O DTO de resposta retornado contém os mesmos dados da tarefa encontrada.</li>
     * </ul>
     */
    @Test
    void buscarPorId_deveRetornarDtoQuandoEncontrado() {
        // Arrange
        var idExistente = 2;
        Tarefa tarefaEsperada = new Tarefa();
        tarefaEsperada.setId(idExistente);
        tarefaEsperada.setTitulo("Title");
        tarefaEsperada.setDescricao("Description");
        tarefaEsperada.setConcluida(false);
        tarefaEsperada.setDataCriacao(Instant.now());

        when(repository.findById(idExistente)).thenReturn(Optional.of(tarefaEsperada));

        // Act
        RespostaTarefaDTO result = service.buscarPorId(idExistente);

        // Assert
        assertNotNull(result);
        assertEquals(tarefaEsperada.getId(), result.id());
        assertEquals(tarefaEsperada.getTitulo(), result.titulo());
        assertEquals(tarefaEsperada.getDescricao(), result.descricao());
        assertEquals(tarefaEsperada.isConcluida(), result.concluida());
        assertEquals(tarefaEsperada.getDataCriacao(), result.dataCriacao());

        verify(repository, times(1)).findById(idExistente);
    }

    /**
     * Testa a busca por uma tarefa com um ID que não existe.
     * <p>
     * <b>Verifica se:</b>
     * <ul>
     * <li>Uma exceção 'NotFoundResponse' é lançada.</li>
     * </ul>
     */
    @Test
    void buscarPorId_deveLancarExcecaoSeNaoEncontrado() {
        int idInexistente = 999;

        when(repository.findById(idInexistente)).thenReturn(Optional.empty());

        var exception = assertThrows(NotFoundResponse.class, () ->
                service.buscarPorId(idInexistente));

        assertEquals("Tarefa não encontrada com o ID: " + idInexistente, exception.getMessage());
        verify(repository).findById(anyInt());
    }

    /**
     * Testa o cenário de sucesso da exclusão de uma tarefa.
     * <p>
     * <b>Verifica se:</b>
     * <ul>
     * <li>O serviço primeiro busca a tarefa para garantir que ela existe.</li>
     * <li>O método 'delete' do repositório é chamado com o ID correto.</li>
     * </ul>
     */
    @Test
    void deletar_deveChamarRepositorioQuandoTarefaExiste() {
        var expectedId = 1;

        CriarTarefaDTO dto = new CriarTarefaDTO("Teste", "Testando");

        // Cria um objeto Tarefa para simular o que seria retornado pelo banco.
        Tarefa tarefaSalva = new Tarefa();
        tarefaSalva.setId(1);
        tarefaSalva.setTitulo(dto.titulo());
        tarefaSalva.setDescricao(dto.descricao());
        tarefaSalva.setConcluida(false);
        tarefaSalva.setDataCriacao(Instant.now());

        when(repository.insert(any(Tarefa.class))).thenReturn(1);

        when(repository.findById(1)).thenReturn(Optional.of(tarefaSalva));

        RespostaTarefaDTO result = service.criar(dto);

        doNothing().when(repository).delete(expectedId);

        service.deletar(expectedId);

        verify(repository, times(1)).delete(anyInt());
    }

}