package br.com.fiap.elosolidario.dto;

import br.com.fiap.elosolidario.model.Alimento;
import br.com.fiap.elosolidario.model.Conservacao;

import java.math.BigDecimal;

public record AlimentoResponse(Long id, String nome, Long idCategoria, String categoria, Boolean perecivel,
                               Conservacao conservacao, BigDecimal calorias100g) {

    public static AlimentoResponse de(Alimento alimento) {
        return new AlimentoResponse(alimento.getId(), alimento.getNome(), alimento.getCategoria().getId(),
                alimento.getCategoria().getNome(), alimento.getCategoria().getPerecivel(),
                alimento.getConservacao(), alimento.getCalorias100g());
    }
}
