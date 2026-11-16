package br.com.fiap.elosolidario.messaging;

import br.com.fiap.elosolidario.model.TipoNotificacao;

public record ReservaEvento(TipoNotificacao tipo, Long idReserva, Long idDoacao, String tituloDoacao,
                            String localRetirada, Long idDoador, String nomeDoador, Long idBeneficiario,
                            String nomeBeneficiario) {
}
