package br.com.fiap.elosolidario.controller;

import br.com.fiap.elosolidario.dto.ReservaRequest;
import br.com.fiap.elosolidario.dto.ReservaResponse;
import br.com.fiap.elosolidario.model.Usuario;
import br.com.fiap.elosolidario.service.ReservaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Reservas", description = "Reserva, retirada e cancelamento das doações")
public class ReservaController {

    private final ReservaService reservaService;

    @PostMapping("/doacoes/{idDoacao}/reservas")
    @PreAuthorize("hasRole('BENEFICIARIO')")
    @Operation(summary = "Reserva uma doação disponível")
    public ResponseEntity<ReservaResponse> reservar(@PathVariable Long idDoacao,
                                                    @RequestBody(required = false) @Valid ReservaRequest request,
                                                    @AuthenticationPrincipal Usuario beneficiario) {
        String observacao = request == null ? null : request.observacao();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservaService.reservar(idDoacao, beneficiario, observacao));
    }

    @GetMapping("/doacoes/{idDoacao}/reservas")
    @PreAuthorize("hasAnyRole('DOADOR', 'ADMIN')")
    @Operation(summary = "Histórico de reservas de uma doação (somente o doador responsável ou admin)")
    public List<ReservaResponse> reservasDaDoacao(@PathVariable Long idDoacao, @AuthenticationPrincipal Usuario usuario) {
        return reservaService.listarDaDoacao(idDoacao, usuario);
    }

    @GetMapping("/reservas/minhas")
    @PreAuthorize("hasRole('BENEFICIARIO')")
    @Operation(summary = "Reservas do beneficiário logado")
    public Page<ReservaResponse> minhas(@AuthenticationPrincipal Usuario beneficiario,
                                        @PageableDefault(size = 10, sort = "dtReserva", direction = Sort.Direction.DESC)
                                        Pageable pageable) {
        return reservaService.listarDoBeneficiario(beneficiario, pageable);
    }

    @PatchMapping("/reservas/{id}/concluir")
    @PreAuthorize("hasAnyRole('DOADOR', 'ADMIN')")
    @Operation(summary = "Doador confirma que o alimento foi retirado")
    public ReservaResponse concluir(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        return reservaService.concluir(id, usuario);
    }

    @PatchMapping("/reservas/{id}/cancelar")
    @Operation(summary = "Cancela uma reserva pendente (beneficiário, doador ou admin)")
    public ReservaResponse cancelar(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        return reservaService.cancelar(id, usuario);
    }
}
