package br.com.fiap.elosolidario.repository;

import br.com.fiap.elosolidario.model.Doacao;
import br.com.fiap.elosolidario.model.StatusDoacao;
import br.com.fiap.elosolidario.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DoacaoRepository extends JpaRepository<Doacao, Long> {

    @Query(value = """
            select d from Doacao d
             where (:status is null or d.status = :status)
               and (:bairro is null or lower(d.bairro) = lower(:bairro))
               and (:idCategoria is null or exists (
                        select 1 from ItemDoacao i
                         where i.doacao = d and i.alimento.categoria.id = :idCategoria))
            """)
    Page<Doacao> buscar(@Param("status") StatusDoacao status,
                        @Param("bairro") String bairro,
                        @Param("idCategoria") Long idCategoria,
                        Pageable pageable);

    Page<Doacao> findByDoador(Usuario doador, Pageable pageable);
}
