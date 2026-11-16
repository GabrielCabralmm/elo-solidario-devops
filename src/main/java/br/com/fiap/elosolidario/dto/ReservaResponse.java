package br.com.fiap.elosolidario.dto;

import br.com.fiap.elosolidario.model.Reserva;
import br.com.fiap.elosolidario.model.StatusReserva;

import java.time.LocalDateTime;

public record ReservaResponse(Long id, Long idDoacao, String doacao, String localRetirada, Long idBeneficiario,
                              String beneficiario, StatusReserva status, LocalDateTime dtReserva,
                              LocalDateTime dtRetirada, String observacao) {

    public static ReservaResponse de(Reserva reserva) {
        return new ReservaResponse(reserva.getId(), reserva.getDoacao().getId(), reserva.getDoacao().getTitulo(),
                reserva.getDoacao().getLocalRetirada(), reserva.getBeneficiario().getId(),
                reserva.getBeneficiario().getNome(), reserva.getStatus(), reserva.getDtReserva(),
                reserva.getDtRetirada(), reserva.getObservacao());
    }
}
