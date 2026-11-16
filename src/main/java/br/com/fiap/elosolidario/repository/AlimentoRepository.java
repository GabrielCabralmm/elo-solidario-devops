package br.com.fiap.elosolidario.repository;

import br.com.fiap.elosolidario.model.Alimento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AlimentoRepository extends JpaRepository<Alimento, Long> {

    List<Alimento> findByAtivoTrueOrderByNome();

    @Query("""
            select a from Alimento a
             where a.ativo = true
               and (:idCategoria is null or a.categoria.id = :idCategoria)
               and (:nome is null or lower(a.nome) like lower(concat('%', :nome, '%')))
            """)
    Page<Alimento> buscar(@Param("idCategoria") Long idCategoria, @Param("nome") String nome, Pageable pageable);
}
