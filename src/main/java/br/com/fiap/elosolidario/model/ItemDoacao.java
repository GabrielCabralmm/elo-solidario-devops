package br.com.fiap.elosolidario.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "TB_ELO_ITEM_DOACAO")
@Getter
@Setter
@NoArgsConstructor
public class ItemDoacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ITEM")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_DOACAO")
    private Doacao doacao;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_ALIMENTO")
    private Alimento alimento;

    private BigDecimal quantidade;

    @Enumerated(EnumType.STRING)
    private Unidade unidade;

    @Column(name = "PESO_KG")
    private BigDecimal pesoKg;

    @Column(name = "DT_VALIDADE")
    private LocalDate dtValidade;
}
