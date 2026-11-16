package br.com.fiap.elosolidario.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "TB_ELO_ALIMENTO")
@Getter
@Setter
@NoArgsConstructor
public class Alimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ALIMENTO")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_CATEGORIA")
    private Categoria categoria;

    private String nome;

    @Enumerated(EnumType.STRING)
    private Conservacao conservacao;

    @Column(name = "CALORIAS_100G")
    private BigDecimal calorias100g;

    @Convert(converter = SimNaoConverter.class)
    private Boolean ativo;
}
