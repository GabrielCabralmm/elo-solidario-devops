package br.com.fiap.elosolidario.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Entity
@Table(name = "TB_ELO_DOACAO")
@Getter
@Setter
@NoArgsConstructor
public class Doacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_DOACAO")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_DOADOR")
    private Usuario doador;

    private String titulo;

    private String bairro;

    @Column(name = "LOCAL_RETIRADA")
    private String localRetirada;

    private String observacao;

    @Enumerated(EnumType.STRING)
    private StatusDoacao status;

    @Column(name = "DT_PUBLICACAO")
    private LocalDateTime dtPublicacao;

    @OneToMany(mappedBy = "doacao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemDoacao> itens = new ArrayList<>();

    @OneToMany(mappedBy = "doacao")
    private List<Reserva> reservas = new ArrayList<>();

    public void adicionarItem(ItemDoacao item) {
        item.setDoacao(this);
        itens.add(item);
    }

    public BigDecimal getPesoTotalKg() {
        return itens.stream()
                .map(ItemDoacao::getPesoKg)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public LocalDate getVenceEm() {
        return itens.stream()
                .map(ItemDoacao::getDtValidade)
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    public boolean possuiItemVencido() {
        return itens.stream().anyMatch(item -> item.getDtValidade().isBefore(LocalDate.now()));
    }
}
