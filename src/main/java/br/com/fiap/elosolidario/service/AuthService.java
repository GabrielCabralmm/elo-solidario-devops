package br.com.fiap.elosolidario.service;

import br.com.fiap.elosolidario.dto.LoginRequest;
import br.com.fiap.elosolidario.dto.RegistroRequest;
import br.com.fiap.elosolidario.dto.TokenResponse;
import br.com.fiap.elosolidario.dto.UsuarioResponse;
import br.com.fiap.elosolidario.exception.RegraNegocioException;
import br.com.fiap.elosolidario.model.Perfil;
import br.com.fiap.elosolidario.model.Usuario;
import br.com.fiap.elosolidario.repository.UsuarioRepository;
import br.com.fiap.elosolidario.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public TokenResponse registrar(RegistroRequest request) {
        if (request.perfil() == Perfil.ADMIN) {
            throw new RegraNegocioException("Não é permitido criar usuários administradores pelo cadastro");
        }
        if (usuarioRepository.existsByEmailIgnoreCase(request.email())) {
            throw new RegraNegocioException("Já existe um usuário cadastrado com este e-mail");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(request.nome().trim());
        usuario.setEmail(request.email().trim().toLowerCase());
        usuario.setSenha(passwordEncoder.encode(request.senha()));
        usuario.setPerfil(request.perfil());
        usuario.setTelefone(request.telefone());
        usuario.setBairro(request.bairro().trim());
        usuario.setDtCadastro(LocalDateTime.now());
        usuarioRepository.save(usuario);

        return gerarResposta(usuario);
    }

    public TokenResponse login(LoginRequest request) {
        var autenticacao = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.senha()));
        return gerarResposta((Usuario) autenticacao.getPrincipal());
    }

    private TokenResponse gerarResposta(Usuario usuario) {
        return new TokenResponse(jwtService.gerarToken(usuario), "Bearer", jwtService.getExpiracaoSegundos(),
                UsuarioResponse.de(usuario));
    }
}
