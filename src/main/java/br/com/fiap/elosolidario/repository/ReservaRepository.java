package br.com.fiap.elosolidario.repository;

import br.com.fiap.elosolidario.model.Doacao;
import br.com.fiap.elosolidario.model.Reserva;
import br.com.fiap.elosolidario.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    Page<Reserva> findByBeneficiario(Usuario beneficiario, Pageable pageable);

    List<Reserva> findByDoacaoOrderByDtReservaDesc(Doacao doacao);

    boolean existsByDoacao(Doacao doacao);
}
