package br.com.fiap.elosolidario.controller;

import br.com.fiap.elosolidario.dto.SugestaoRequest;
import br.com.fiap.elosolidario.dto.SugestaoResponse;
import br.com.fiap.elosolidario.service.AssistenteDoacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/assistente")
@RequiredArgsConstructor
@Tag(name = "Assistente IA", description = "Cadastro assistido de doações com Spring AI")
public class AssistenteController {

    private final AssistenteDoacaoService assistenteDoacaoService;

    @PostMapping("/sugestao")
    @PreAuthorize("hasAnyRole('DOADOR', 'ADMIN')")
    @Operation(summary = "Transforma uma descrição em texto livre em itens do catálogo para a doação")
    public SugestaoResponse sugerir(@RequestBody @Valid SugestaoRequest request) {
        return assistenteDoacaoService.sugerir(request.texto());
    }
}
