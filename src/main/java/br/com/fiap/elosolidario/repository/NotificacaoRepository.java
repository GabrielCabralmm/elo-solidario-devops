package br.com.fiap.elosolidario.repository;

import br.com.fiap.elosolidario.model.Notificacao;
import br.com.fiap.elosolidario.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    Page<Notificacao> findByUsuario(Usuario usuario, Pageable pageable);

    long countByUsuarioAndLidaFalse(Usuario usuario);
}
