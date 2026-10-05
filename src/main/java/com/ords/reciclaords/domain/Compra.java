package com.ords.reciclaords.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Compra de materiais feita pela empresa junto a um cliente (entrada de estoque)
@Entity
@Table(name = "compras")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Column(nullable = false)
    private LocalDateTime data;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CompraItem> itens = new ArrayList<>();
}
