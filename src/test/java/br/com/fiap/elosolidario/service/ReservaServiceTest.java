package br.com.fiap.elosolidario.service;

import br.com.fiap.elosolidario.exception.AcessoNegadoException;
import br.com.fiap.elosolidario.exception.RegraNegocioException;
import br.com.fiap.elosolidario.model.*;
import br.com.fiap.elosolidario.repository.ReservaRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private DoacaoService doacaoService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private ReservaService reservaService;

    private Usuario doador;
    private Usuario beneficiario;
    private Doacao doacao;

    @BeforeEach
    void setUp() {
        doador = usuario(1L, Perfil.DOADOR);
        beneficiario = usuario(5L, Perfil.BENEFICIARIO);
        doacao = doacao(LocalDate.now().plusDays(10));
    }

    @Test
    void deveReservarDoacaoDisponivelEPublicarEvento() {
        var resposta = reservaService.reservar(10L, beneficiario, "Passo às 18h");

        assertThat(resposta.status()).isEqualTo(StatusReserva.PENDENTE);
        assertThat(resposta.beneficiario()).isEqualTo(beneficiario.getNome());
        verify(reservaRepository).saveAndFlush(any(Reserva.class));
        verify(eventPublisher).publishEvent(any(Object.class));
    }

    @Test
    void naoDeveReservarDoacaoJaReservada() {
        doacao.setStatus(StatusDoacao.RESERVADA);

        assertThatThrownBy(() -> reservaService.reservar(10L, beneficiario, null))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("não está disponível");
        verify(reservaRepository, never()).saveAndFlush(any());
    }

    @Test
    void naoDeveReservarDoacaoComAlimentoVencido() {
        doacao = doacao(LocalDate.now().minusDays(1));

        assertThatThrownBy(() -> reservaService.reservar(10L, beneficiario, null))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("validade vencida");
    }

    @Test
    void naoDeveConcluirReservaJaFinalizada() {
        Reserva reserva = reserva(StatusReserva.CONCLUIDA);
        when(reservaRepository.findById(20L)).thenReturn(Optional.of(reserva));

        assertThatThrownBy(() -> reservaService.concluir(20L, doador))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("já foi finalizada");
    }

    @Test
    void terceiroNaoPodeCancelarReserva() {
        Reserva reserva = reserva(StatusReserva.PENDENTE);
        when(reservaRepository.findById(20L)).thenReturn(Optional.of(reserva));

        assertThatThrownBy(() -> reservaService.cancelar(20L, usuario(99L, Perfil.BENEFICIARIO)))
                .isInstanceOf(AcessoNegadoException.class);
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    private Usuario usuario(Long id, Perfil perfil) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome("Usuário " + id);
        usuario.setPerfil(perfil);
        return usuario;
    }

    private Doacao doacao(LocalDate validade) {
        Doacao nova = new Doacao();
        nova.setId(10L);
        nova.setTitulo("Cesta básica");
        nova.setDoador(doador);
        nova.setStatus(StatusDoacao.DISPONIVEL);

        ItemDoacao item = new ItemDoacao();
        item.setPesoKg(BigDecimal.ONE);
        item.setDtValidade(validade);
        nova.adicionarItem(item);

        lenient().when(doacaoService.buscarEntidade(10L)).thenReturn(nova);
        return nova;
    }

    private Reserva reserva(StatusReserva status) {
        Reserva reserva = new Reserva();
        reserva.setId(20L);
        reserva.setDoacao(doacao);
        reserva.setBeneficiario(beneficiario);
        reserva.setStatus(status);
        return reserva;
    }
}
