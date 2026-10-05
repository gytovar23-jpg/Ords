package com.ords.reciclaords.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "venda_itens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "venda_id")
    private Venda venda;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id")
    private Material material;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal pesoKg;

    // Preço por kg negociado na hora
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precoKg;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal;
}
