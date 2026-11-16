package br.com.fiap.elosolidario.dto;

import br.com.fiap.elosolidario.model.Unidade;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ItemDoacaoRequest(
        @NotNull(message = "O alimento é obrigatório")
        Long idAlimento,

        @NotNull(message = "A quantidade é obrigatória")
        @Positive(message = "A quantidade deve ser maior que zero")
        BigDecimal quantidade,

        @NotNull(message = "A unidade é obrigatória (UN, KG, L ou PCT)")
        Unidade unidade,

        @NotNull(message = "O peso é obrigatório")
        @Positive(message = "O peso deve ser maior que zero")
        @DecimalMax(value = "500", message = "O peso de um item não pode passar de 500 kg")
        BigDecimal pesoKg,

        @NotNull(message = "A data de validade é obrigatória")
        @FutureOrPresent(message = "Não é permitido doar alimento com a validade vencida")
        LocalDate dtValidade
) {
}
