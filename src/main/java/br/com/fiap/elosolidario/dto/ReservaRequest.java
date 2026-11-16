package br.com.fiap.elosolidario.dto;

import jakarta.validation.constraints.Size;

public record ReservaRequest(
        @Size(max = 200, message = "A observação deve ter no máximo 200 caracteres")
        String observacao
) {
}
