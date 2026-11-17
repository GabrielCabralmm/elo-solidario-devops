package br.com.fiap.elosolidario.controller;

import br.com.fiap.elosolidario.dto.AlimentoResponse;
import br.com.fiap.elosolidario.dto.CategoriaRequest;
import br.com.fiap.elosolidario.dto.CategoriaResponse;
import br.com.fiap.elosolidario.service.CatalogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Catálogo", description = "Categorias e alimentos que podem ser doados")
public class CatalogoController {

    private final CatalogoService catalogoService;

    @GetMapping("/categorias")
    @Operation(summary = "Lista as categorias de alimento")
    public List<CategoriaResponse> categorias() {
        return catalogoService.listarCategorias();
    }

    @GetMapping("/categorias/{id}")
    @Operation(summary = "Busca uma categoria")
    public CategoriaResponse categoria(@PathVariable Long id) {
        return catalogoService.buscarCategoria(id);
    }

    @PostMapping("/categorias")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cadastra uma categoria (somente ADMIN)")
    public ResponseEntity<CategoriaResponse> criarCategoria(@RequestBody @Valid CategoriaRequest request) {
        CategoriaResponse categoria = catalogoService.criarCategoria(request);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(categoria.id()).toUri();
        return ResponseEntity.created(uri).body(categoria);
    }

    @PutMapping("/categorias/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza uma categoria (somente ADMIN)")
    public CategoriaResponse atualizarCategoria(@PathVariable Long id, @RequestBody @Valid CategoriaRequest request) {
        return catalogoService.atualizarCategoria(id, request);
    }

    @DeleteMapping("/categorias/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Exclui uma categoria sem alimentos (somente ADMIN)")
    public ResponseEntity<Void> excluirCategoria(@PathVariable Long id) {
        catalogoService.excluirCategoria(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/alimentos")
    @Operation(summary = "Lista os alimentos ativos do catálogo, com filtro por categoria e nome")
    public Page<AlimentoResponse> alimentos(@RequestParam(required = false) Long idCategoria,
                                            @RequestParam(required = false) String nome,
                                            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return catalogoService.listarAlimentos(idCategoria, nome, pageable);
    }

    @GetMapping("/alimentos/{id}")
    @Operation(summary = "Busca um alimento do catálogo")
    public AlimentoResponse alimento(@PathVariable Long id) {
        return catalogoService.buscarAlimento(id);
    }
}
