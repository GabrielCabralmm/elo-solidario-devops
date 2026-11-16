package br.com.fiap.elosolidario.dto;

public record TokenResponse(String token, String tipo, long expiraEmSegundos, UsuarioResponse usuario) {
}
