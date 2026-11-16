package br.com.fiap.elosolidario.dto;

import br.com.fiap.elosolidario.model.Doacao;
import br.com.fiap.elosolidario.model.StatusDoacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record DoacaoResponse(Long id, String titulo, String bairro, String localRetirada, String observacao,
                             StatusDoacao status, LocalDateTime dtPublicacao, Long idDoador, String doador,
                             BigDecimal pesoTotalKg, LocalDate venceEm, List<ItemDoacaoResponse> itens) {

    public static DoacaoResponse de(Doacao doacao) {
        return new DoacaoResponse(doacao.getId(), doacao.getTitulo(), doacao.getBairro(), doacao.getLocalRetirada(),
                doacao.getObservacao(), doacao.getStatus(), doacao.getDtPublicacao(), doacao.getDoador().getId(),
                doacao.getDoador().getNome(), doacao.getPesoTotalKg(), doacao.getVenceEm(),
                doacao.getItens().stream().map(ItemDoacaoResponse::de).toList());
    }
}
