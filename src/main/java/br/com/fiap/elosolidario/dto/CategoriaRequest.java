package br.com.fiap.elosolidario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
        @NotBlank(message = "O nome é obrigatório")
        @Size(min = 3, max = 60, message = "O nome deve ter entre 3 e 60 caracteres")
        String nome,

        @NotNull(message = "Informe se a categoria é perecível")
        Boolean perecivel
) {
}
