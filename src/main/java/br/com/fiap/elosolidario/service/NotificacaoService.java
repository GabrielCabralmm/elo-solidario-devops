package br.com.fiap.elosolidario.service;

import br.com.fiap.elosolidario.dto.NotificacaoResponse;
import br.com.fiap.elosolidario.exception.AcessoNegadoException;
import br.com.fiap.elosolidario.exception.RecursoNaoEncontradoException;
import br.com.fiap.elosolidario.messaging.ReservaEvento;
import br.com.fiap.elosolidario.model.Notificacao;
import br.com.fiap.elosolidario.model.Usuario;
import br.com.fiap.elosolidario.repository.NotificacaoRepository;
import br.com.fiap.elosolidario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public void registrar(ReservaEvento evento) {
        switch (evento.tipo()) {
            case RESERVA_CRIADA -> salvar(evento, evento.idDoador(),
                    evento.nomeBeneficiario() + " reservou a sua doação \"" + evento.tituloDoacao()
                            + "\". Combine a retirada em: " + evento.localRetirada() + ".");
            case RESERVA_CONCLUIDA -> salvar(evento, evento.idBeneficiario(),
                    "Retirada da doação \"" + evento.tituloDoacao() + "\" confirmada por " + evento.nomeDoador()
                            + ". Bom proveito!");
            case RESERVA_CANCELADA -> salvar(evento, evento.idDoador(),
                    "A reserva da doação \"" + evento.tituloDoacao()
                            + "\" foi cancelada e ela voltou para a vitrine.");
        }
    }

    @Transactional(readOnly = true)
    public Page<NotificacaoResponse> listar(Usuario usuario, Pageable pageable) {
        return notificacaoRepository.findByUsuario(usuario, pageable).map(NotificacaoResponse::de);
    }

    @Transactional
    public NotificacaoResponse marcarComoLida(Long id, Usuario usuario) {
        Notificacao notificacao = notificacaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Notificação " + id + " não encontrada"));

        if (!notificacao.getUsuario().getId().equals(usuario.getId())) {
            throw new AcessoNegadoException("Esta notificação pertence a outro usuário");
        }

        notificacao.setLida(true);
        return NotificacaoResponse.de(notificacao);
    }

    private void salvar(ReservaEvento evento, Long idDestinatario, String mensagem) {
        Notificacao notificacao = new Notificacao();
        notificacao.setUsuario(usuarioRepository.getReferenceById(idDestinatario));
        notificacao.setIdReserva(evento.idReserva());
        notificacao.setTipo(evento.tipo());
        notificacao.setMensagem(mensagem.length() > 300 ? mensagem.substring(0, 300) : mensagem);
        notificacao.setLida(false);
        notificacao.setDtCriacao(LocalDateTime.now());
        notificacaoRepository.save(notificacao);
    }
}
