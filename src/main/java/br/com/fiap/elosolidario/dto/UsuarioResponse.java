package br.com.fiap.elosolidario.dto;

import br.com.fiap.elosolidario.model.Perfil;
import br.com.fiap.elosolidario.model.Usuario;

public record UsuarioResponse(Long id, String nome, String email, Perfil perfil, String telefone, String bairro) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(),
                usuario.getPerfil(), usuario.getTelefone(), usuario.getBairro());
    }
}
