package br.com.fiap.elosolidario.dto;

import br.com.fiap.elosolidario.model.Perfil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 6, max = 40, message = "A senha deve ter entre 6 e 40 caracteres")
        String senha,

        @NotNull(message = "O perfil é obrigatório (DOADOR ou BENEFICIARIO)")
        Perfil perfil,

        @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres")
        String telefone,

        @NotBlank(message = "O bairro é obrigatório")
        @Size(max = 60, message = "O bairro deve ter no máximo 60 caracteres")
        String bairro
) {
}
