package br.com.fiap.elosolidario.service;

import br.com.fiap.elosolidario.dto.CategoriaRequest;
import br.com.fiap.elosolidario.exception.RecursoNaoEncontradoException;
import br.com.fiap.elosolidario.exception.RegraNegocioException;
import br.com.fiap.elosolidario.model.Categoria;
import br.com.fiap.elosolidario.repository.AlimentoRepository;
import br.com.fiap.elosolidario.repository.CategoriaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogoServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private AlimentoRepository alimentoRepository;

    @InjectMocks
    private CatalogoService catalogoService;

    @Test
    void deveCriarCategoriaComNomeSemEspacosExtras() {
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        var resposta = catalogoService.criarCategoria(new CategoriaRequest("  Bebidas  ", false));

        assertThat(resposta.nome()).isEqualTo("Bebidas");
        assertThat(resposta.perecivel()).isFalse();
    }

    @Test
    void naoDeveCriarCategoriaDuplicada() {
        when(categoriaRepository.existsByNomeIgnoreCase("Enlatados")).thenReturn(true);

        assertThatThrownBy(() -> catalogoService.criarCategoria(new CategoriaRequest("Enlatados", false)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Já existe");
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void naoDeveExcluirCategoriaQuePossuiAlimentos() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(new Categoria()));
        when(alimentoRepository.existsByCategoriaId(1L)).thenReturn(true);

        assertThatThrownBy(() -> catalogoService.excluirCategoria(1L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("possui alimentos");
        verify(categoriaRepository, never()).delete(any());
    }

    @Test
    void deveRetornarNaoEncontradoParaCategoriaInexistente() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalogoService.atualizarCategoria(99L, new CategoriaRequest("Doces", false)))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
