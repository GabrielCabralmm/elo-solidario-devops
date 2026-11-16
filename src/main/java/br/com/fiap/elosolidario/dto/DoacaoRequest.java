package br.com.fiap.elosolidario.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record DoacaoRequest(
        @NotBlank(message = "O título é obrigatório")
        @Size(max = 100, message = "O título deve ter no máximo 100 caracteres")
        String titulo,

        @NotBlank(message = "O bairro é obrigatório")
        @Size(max = 60, message = "O bairro deve ter no máximo 60 caracteres")
        String bairro,

        @NotBlank(message = "O local de retirada é obrigatório")
        @Size(max = 150, message = "O local de retirada deve ter no máximo 150 caracteres")
        String localRetirada,

        @Size(max = 300, message = "A observação deve ter no máximo 300 caracteres")
        String observacao,

        @NotEmpty(message = "Informe pelo menos um alimento")
        @Size(max = 20, message = "Uma doação pode ter no máximo 20 alimentos")
        List<@Valid ItemDoacaoRequest> itens
) {
}
