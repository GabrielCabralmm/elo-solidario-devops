package br.com.fiap.elosolidario.service;

import br.com.fiap.elosolidario.dto.ReservaResponse;
import br.com.fiap.elosolidario.exception.AcessoNegadoException;
import br.com.fiap.elosolidario.exception.RecursoNaoEncontradoException;
import br.com.fiap.elosolidario.exception.RegraNegocioException;
import br.com.fiap.elosolidario.messaging.ReservaEvento;
import br.com.fiap.elosolidario.model.*;
import br.com.fiap.elosolidario.repository.ReservaRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final DoacaoService doacaoService;
    private final ApplicationEventPublisher eventPublisher;
    private final EntityManager entityManager;

    @Transactional
    public ReservaResponse reservar(Long idDoacao, Usuario beneficiario, String observacao) {
        Doacao doacao = doacaoService.buscarEntidade(idDoacao);

        if (doacao.getStatus() != StatusDoacao.DISPONIVEL) {
            throw new RegraNegocioException("A doação não está disponível para reserva. Status atual: "
                    + doacao.getStatus());
        }
        if (doacao.possuiItemVencido()) {
            throw new RegraNegocioException("A doação possui alimento com a validade vencida e não pode ser reservada");
        }

        Reserva reserva = new Reserva();
        reserva.setDoacao(doacao);
        reserva.setBeneficiario(beneficiario);
        reserva.setDtReserva(LocalDateTime.now());
        reserva.setStatus(StatusReserva.PENDENTE);
        reserva.setObservacao(observacao);
        reservaRepository.saveAndFlush(reserva);

        entityManager.refresh(doacao);
        publicarEvento(TipoNotificacao.RESERVA_CRIADA, reserva);
        return ReservaResponse.de(reserva);
    }

    @Transactional
    public ReservaResponse concluir(Long idReserva, Usuario usuario) {
        Reserva reserva = buscarPendente(idReserva);
        doacaoService.validarDono(reserva.getDoacao(), usuario);

        reserva.setStatus(StatusReserva.CONCLUIDA);
        reserva.setDtRetirada(LocalDateTime.now());
        reservaRepository.saveAndFlush(reserva);

        entityManager.refresh(reserva.getDoacao());
        publicarEvento(TipoNotificacao.RESERVA_CONCLUIDA, reserva);
        return ReservaResponse.de(reserva);
    }

    @Transactional
    public ReservaResponse cancelar(Long idReserva, Usuario usuario) {
        Reserva reserva = buscarPendente(idReserva);

        boolean ehBeneficiario = reserva.getBeneficiario().getId().equals(usuario.getId());
        boolean ehDoador = reserva.getDoacao().getDoador().getId().equals(usuario.getId());
        if (!ehBeneficiario && !ehDoador && !usuario.isAdmin()) {
            throw new AcessoNegadoException("Somente o beneficiário ou o doador podem cancelar esta reserva");
        }

        reserva.setStatus(StatusReserva.CANCELADA);
        reservaRepository.saveAndFlush(reserva);

        entityManager.refresh(reserva.getDoacao());
        publicarEvento(TipoNotificacao.RESERVA_CANCELADA, reserva);
        return ReservaResponse.de(reserva);
    }

    @Transactional(readOnly = true)
    public Page<ReservaResponse> listarDoBeneficiario(Usuario beneficiario, Pageable pageable) {
        return reservaRepository.findByBeneficiario(beneficiario, pageable).map(ReservaResponse::de);
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> listarDaDoacao(Long idDoacao, Usuario usuario) {
        Doacao doacao = doacaoService.buscarEntidade(idDoacao);
        doacaoService.validarDono(doacao, usuario);
        return reservaRepository.findByDoacaoOrderByDtReservaDesc(doacao).stream().map(ReservaResponse::de).toList();
    }

    private Reserva buscarPendente(Long idReserva) {
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Reserva " + idReserva + " não encontrada"));

        if (reserva.getStatus() != StatusReserva.PENDENTE) {
            throw new RegraNegocioException("A reserva já foi finalizada. Status atual: " + reserva.getStatus());
        }
        return reserva;
    }

    private void publicarEvento(TipoNotificacao tipo, Reserva reserva) {
        Doacao doacao = reserva.getDoacao();
        eventPublisher.publishEvent(new ReservaEvento(tipo, reserva.getId(), doacao.getId(), doacao.getTitulo(),
                doacao.getLocalRetirada(), doacao.getDoador().getId(), doacao.getDoador().getNome(),
                reserva.getBeneficiario().getId(), reserva.getBeneficiario().getNome()));
    }
}
