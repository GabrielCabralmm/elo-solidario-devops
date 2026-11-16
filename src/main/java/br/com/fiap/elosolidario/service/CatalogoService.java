package br.com.fiap.elosolidario.service;

import br.com.fiap.elosolidario.dto.AlimentoResponse;
import br.com.fiap.elosolidario.dto.CategoriaResponse;
import br.com.fiap.elosolidario.exception.RecursoNaoEncontradoException;
import br.com.fiap.elosolidario.repository.AlimentoRepository;
import br.com.fiap.elosolidario.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogoService {

    private final CategoriaRepository categoriaRepository;
    private final AlimentoRepository alimentoRepository;

    public List<CategoriaResponse> listarCategorias() {
        return categoriaRepository.findAll(Sort.by("nome")).stream().map(CategoriaResponse::de).toList();
    }

    public Page<AlimentoResponse> listarAlimentos(Long idCategoria, String nome, Pageable pageable) {
        String filtroNome = nome == null || nome.isBlank() ? null : nome.trim();
        return alimentoRepository.buscar(idCategoria, filtroNome, pageable).map(AlimentoResponse::de);
    }

    public AlimentoResponse buscarAlimento(Long id) {
        return alimentoRepository.findById(id)
                .map(AlimentoResponse::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Alimento " + id + " não encontrado"));
    }
}
