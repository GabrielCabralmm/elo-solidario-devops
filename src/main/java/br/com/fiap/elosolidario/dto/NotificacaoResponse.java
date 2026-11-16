package br.com.fiap.elosolidario.dto;

import br.com.fiap.elosolidario.model.Notificacao;
import br.com.fiap.elosolidario.model.TipoNotificacao;

import java.time.LocalDateTime;

public record NotificacaoResponse(Long id, TipoNotificacao tipo, String mensagem, Boolean lida,
                                  LocalDateTime dtCriacao, Long idReserva) {

    public static NotificacaoResponse de(Notificacao notificacao) {
        return new NotificacaoResponse(notificacao.getId(), notificacao.getTipo(), notificacao.getMensagem(),
                notificacao.getLida(), notificacao.getDtCriacao(), notificacao.getIdReserva());
    }
}
