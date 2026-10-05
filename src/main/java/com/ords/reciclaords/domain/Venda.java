package com.ords.reciclaords.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Venda do estoque feita pela empresa (saída de estoque)
@Entity
@Table(name = "vendas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String comprador;

    @Column(nullable = false)
    private LocalDateTime data;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal;

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<VendaItem> itens = new ArrayList<>();
}
