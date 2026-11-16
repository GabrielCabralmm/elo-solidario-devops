package br.com.fiap.elosolidario.controller;

import br.com.fiap.elosolidario.dto.DoacaoRequest;
import br.com.fiap.elosolidario.dto.DoacaoResponse;
import br.com.fiap.elosolidario.model.StatusDoacao;
import br.com.fiap.elosolidario.model.Usuario;
import br.com.fiap.elosolidario.service.DoacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/doacoes")
@RequiredArgsConstructor
@Tag(name = "Doações", description = "Publicação e vitrine de doações de alimentos")
public class DoacaoController {

    private final DoacaoService doacaoService;

    @GetMapping
    @Operation(summary = "Vitrine de doações com filtros por status, bairro e categoria (padrão: disponíveis)")
    public Page<DoacaoResponse> listar(@RequestParam(defaultValue = "DISPONIVEL") StatusDoacao status,
                                       @RequestParam(required = false) String bairro,
                                       @RequestParam(required = false) Long idCategoria,
                                       @PageableDefault(size = 10, sort = "dtPublicacao", direction = Sort.Direction.DESC)
                                       Pageable pageable) {
        return doacaoService.listar(status, bairro, idCategoria, pageable);
    }

    @GetMapping("/minhas")
    @PreAuthorize("hasRole('DOADOR')")
    @Operation(summary = "Doações publicadas pelo doador logado")
    public Page<DoacaoResponse> minhas(@AuthenticationPrincipal Usuario doador,
                                       @PageableDefault(size = 10, sort = "dtPublicacao", direction = Sort.Direction.DESC)
                                       Pageable pageable) {
        return doacaoService.listarDoDoador(doador, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhes de uma doação com seus alimentos")
    public DoacaoResponse buscar(@PathVariable Long id) {
        return doacaoService.buscar(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DOADOR', 'ADMIN')")
    @Operation(summary = "Publica uma doação com um ou mais alimentos do catálogo")
    public ResponseEntity<DoacaoResponse> criar(@RequestBody @Valid DoacaoRequest request,
                                                @AuthenticationPrincipal Usuario doador) {
        DoacaoResponse doacao = doacaoService.criar(request, doador);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(doacao.id()).toUri();
        return ResponseEntity.created(uri).body(doacao);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOADOR', 'ADMIN')")
    @Operation(summary = "Atualiza uma doação disponível (somente o doador responsável ou admin)")
    public DoacaoResponse atualizar(@PathVariable Long id, @RequestBody @Valid DoacaoRequest request,
                                    @AuthenticationPrincipal Usuario usuario) {
        return doacaoService.atualizar(id, request, usuario);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOADOR', 'ADMIN')")
    @Operation(summary = "Exclui uma doação disponível que ainda não teve reservas")
    public ResponseEntity<Void> excluir(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        doacaoService.excluir(id, usuario);
        return ResponseEntity.noContent().build();
    }
}
