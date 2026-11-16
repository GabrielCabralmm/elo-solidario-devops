package br.com.fiap.elosolidario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SugestaoRequest(
        @NotBlank(message = "Descreva os alimentos que deseja doar")
        @Size(min = 5, max = 500, message = "A descrição deve ter entre 5 e 500 caracteres")
        String texto
) {
}
