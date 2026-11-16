package br.com.fiap.elosolidario.controller;

import br.com.fiap.elosolidario.dto.AlimentoResponse;
import br.com.fiap.elosolidario.dto.CategoriaResponse;
import br.com.fiap.elosolidario.service.CatalogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
