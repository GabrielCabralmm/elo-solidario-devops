package br.com.fiap.elosolidario.dto;

import br.com.fiap.elosolidario.model.Conservacao;
import br.com.fiap.elosolidario.model.ItemDoacao;
import br.com.fiap.elosolidario.model.Unidade;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ItemDoacaoResponse(Long id, Long idAlimento, String alimento, String categoria, Conservacao conservacao,
                                 BigDecimal quantidade, Unidade unidade, BigDecimal pesoKg, LocalDate dtValidade) {

    public static ItemDoacaoResponse de(ItemDoacao item) {
        return new ItemDoacaoResponse(item.getId(), item.getAlimento().getId(), item.getAlimento().getNome(),
                item.getAlimento().getCategoria().getNome(), item.getAlimento().getConservacao(),
                item.getQuantidade(), item.getUnidade(), item.getPesoKg(), item.getDtValidade());
    }
}
