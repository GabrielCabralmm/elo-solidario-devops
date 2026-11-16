package br.com.fiap.elosolidario.controller;

import br.com.fiap.elosolidario.dto.LoginRequest;
import br.com.fiap.elosolidario.dto.RegistroRequest;
import br.com.fiap.elosolidario.dto.TokenResponse;
import br.com.fiap.elosolidario.dto.UsuarioResponse;
import br.com.fiap.elosolidario.model.Usuario;
import br.com.fiap.elosolidario.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Cadastro, login e dados do usuário logado")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/registrar")
    @Operation(summary = "Cadastra um doador ou beneficiário e devolve o token JWT")
    public ResponseEntity<TokenResponse> registrar(@RequestBody @Valid RegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Autentica com e-mail e senha e devolve o token JWT")
    public TokenResponse login(@RequestBody @Valid LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @Operation(summary = "Retorna os dados do usuário dono do token")
    public UsuarioResponse me(@AuthenticationPrincipal Usuario usuario) {
        return UsuarioResponse.de(usuario);
    }
}
