package br.com.fiap.elosolidario.dto;

import br.com.fiap.elosolidario.model.Conservacao;
import br.com.fiap.elosolidario.model.Unidade;

import java.math.BigDecimal;
import java.util.List;

public record SugestaoResponse(String tituloSugerido, List<ItemSugerido> itens, List<String> naoReconhecidos,
                               List<String> orientacoes, String aviso) {

    public record ItemSugerido(Long idAlimento, String alimento, String categoria, Conservacao conservacao,
                               BigDecimal quantidade, Unidade unidade, BigDecimal pesoKgEstimado) {
    }
}
