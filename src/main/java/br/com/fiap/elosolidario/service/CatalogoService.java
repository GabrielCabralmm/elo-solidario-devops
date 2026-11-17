package br.com.fiap.elosolidario.service;

import br.com.fiap.elosolidario.dto.AlimentoResponse;
import br.com.fiap.elosolidario.dto.CategoriaRequest;
import br.com.fiap.elosolidario.dto.CategoriaResponse;
import br.com.fiap.elosolidario.exception.RecursoNaoEncontradoException;
import br.com.fiap.elosolidario.exception.RegraNegocioException;
import br.com.fiap.elosolidario.model.Categoria;
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

    public CategoriaResponse buscarCategoria(Long id) {
        return CategoriaResponse.de(buscarCategoriaEntidade(id));
    }

    @Transactional
    public CategoriaResponse criarCategoria(CategoriaRequest request) {
        String nome = request.nome().trim();
        if (categoriaRepository.existsByNomeIgnoreCase(nome)) {
            throw new RegraNegocioException("Já existe a categoria '" + nome + "'");
        }

        Categoria categoria = new Categoria();
        categoria.setNome(nome);
        categoria.setPerecivel(request.perecivel());
        return CategoriaResponse.de(categoriaRepository.save(categoria));
    }

    @Transactional
    public CategoriaResponse atualizarCategoria(Long id, CategoriaRequest request) {
        Categoria categoria = buscarCategoriaEntidade(id);
        String nome = request.nome().trim();
        if (categoriaRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new RegraNegocioException("Já existe a categoria '" + nome + "'");
        }

        categoria.setNome(nome);
        categoria.setPerecivel(request.perecivel());
        return CategoriaResponse.de(categoria);
    }

    @Transactional
    public void excluirCategoria(Long id) {
        Categoria categoria = buscarCategoriaEntidade(id);
        if (alimentoRepository.existsByCategoriaId(id)) {
            throw new RegraNegocioException("A categoria possui alimentos no catálogo e não pode ser excluída");
        }
        categoriaRepository.delete(categoria);
    }

    public Page<AlimentoResponse> listarAlimentos(Long idCategoria, String nome, Pageable pageable) {
        String filtroNome = nome == null || nome.isBlank() ? null : nome.trim();
        return alimentoRepository.buscar(idCategoria, filtroNome, pageable).map(AlimentoResponse::de);
    }

    private Categoria buscarCategoriaEntidade(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria " + id + " não encontrada"));
    }

    public AlimentoResponse buscarAlimento(Long id) {
        return alimentoRepository.findById(id)
                .map(AlimentoResponse::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Alimento " + id + " não encontrado"));
    }
}
