package br.com.fiap.elosolidario.service;

import br.com.fiap.elosolidario.dto.DoacaoRequest;
import br.com.fiap.elosolidario.dto.ItemDoacaoRequest;
import br.com.fiap.elosolidario.exception.AcessoNegadoException;
import br.com.fiap.elosolidario.exception.RegraNegocioException;
import br.com.fiap.elosolidario.model.*;
import br.com.fiap.elosolidario.repository.AlimentoRepository;
import br.com.fiap.elosolidario.repository.DoacaoRepository;
import br.com.fiap.elosolidario.repository.ReservaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoacaoServiceTest {

    @Mock
    private DoacaoRepository doacaoRepository;

    @Mock
    private AlimentoRepository alimentoRepository;

    @Mock
    private ReservaRepository reservaRepository;

    @InjectMocks
    private DoacaoService doacaoService;

    @Test
    void deveCriarDoacaoDisponivelComOsItensInformados() {
        when(alimentoRepository.findAllById(any())).thenReturn(List.of(alimento(1L, true)));
        when(doacaoRepository.save(any(Doacao.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        var resposta = doacaoService.criar(request(item(1L)), doador(1L));

        assertThat(resposta.status()).isEqualTo(StatusDoacao.DISPONIVEL);
        assertThat(resposta.itens()).hasSize(1);
        assertThat(resposta.pesoTotalKg()).isEqualByComparingTo("2");
    }

    @Test
    void naoDeveAceitarAlimentoRepetido() {
        assertThatThrownBy(() -> doacaoService.criar(request(item(1L), item(1L)), doador(1L)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("mais de uma vez");
    }

    @Test
    void naoDeveAceitarAlimentoInativo() {
        when(alimentoRepository.findAllById(any())).thenReturn(List.of(alimento(15L, false)));

        assertThatThrownBy(() -> doacaoService.criar(request(item(15L)), doador(1L)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("não está ativo");
    }

    @Test
    void somenteODonoPodeAlterarADoacao() {
        Doacao doacao = new Doacao();
        doacao.setDoador(doador(1L));

        assertThatThrownBy(() -> doacaoService.validarDono(doacao, doador(2L)))
                .isInstanceOf(AcessoNegadoException.class);
    }

    private DoacaoRequest request(ItemDoacaoRequest... itens) {
        return new DoacaoRequest("Cesta", "Mooca", "Portaria", null, List.of(itens));
    }

    private ItemDoacaoRequest item(Long idAlimento) {
        return new ItemDoacaoRequest(idAlimento, BigDecimal.TWO, Unidade.PCT, BigDecimal.TWO, LocalDate.now().plusDays(30));
    }

    private Usuario doador(Long id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome("Doador " + id);
        usuario.setPerfil(Perfil.DOADOR);
        return usuario;
    }

    private Alimento alimento(Long id, boolean ativo) {
        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNome("Grãos e Cereais");
        categoria.setPerecivel(false);

        Alimento alimento = new Alimento();
        alimento.setId(id);
        alimento.setNome("Alimento " + id);
        alimento.setCategoria(categoria);
        alimento.setConservacao(Conservacao.AMBIENTE);
        alimento.setAtivo(ativo);
        return alimento;
    }
}
