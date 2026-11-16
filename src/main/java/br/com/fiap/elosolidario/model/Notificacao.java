package br.com.fiap.elosolidario.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "TB_ELO_NOTIFICACAO")
@Getter
@Setter
@NoArgsConstructor
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_NOTIFICACAO")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_USUARIO")
    private Usuario usuario;

    @Column(name = "ID_RESERVA")
    private Long idReserva;

    @Enumerated(EnumType.STRING)
    private TipoNotificacao tipo;

    private String mensagem;

    @Convert(converter = SimNaoConverter.class)
    private Boolean lida;

    @Column(name = "DT_CRIACAO")
    private LocalDateTime dtCriacao;
}
