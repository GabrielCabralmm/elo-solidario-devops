package br.com.fiap.elosolidario.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "TB_ELO_RESERVA")
@Getter
@Setter
@NoArgsConstructor
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_RESERVA")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_DOACAO")
    private Doacao doacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_BENEFICIARIO")
    private Usuario beneficiario;

    @Column(name = "DT_RESERVA")
    private LocalDateTime dtReserva;

    @Column(name = "DT_RETIRADA")
    private LocalDateTime dtRetirada;

    @Enumerated(EnumType.STRING)
    private StatusReserva status;

    private String observacao;
}
