package br.com.fiap.elosolidario.controller;

import br.com.fiap.elosolidario.dto.NotificacaoResponse;
import br.com.fiap.elosolidario.model.Usuario;
import br.com.fiap.elosolidario.service.NotificacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notificacoes")
@RequiredArgsConstructor
@Tag(name = "Notificações", description = "Avisos gerados pelos eventos de reserva (RabbitMQ)")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    @GetMapping
    @Operation(summary = "Notificações do usuário logado")
    public Page<NotificacaoResponse> listar(@AuthenticationPrincipal Usuario usuario,
                                            @PageableDefault(size = 20, sort = "dtCriacao", direction = Sort.Direction.DESC)
                                            Pageable pageable) {
        return notificacaoService.listar(usuario, pageable);
    }

    @PatchMapping("/{id}/lida")
    @Operation(summary = "Marca uma notificação como lida")
    public NotificacaoResponse marcarComoLida(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        return notificacaoService.marcarComoLida(id, usuario);
    }
}
