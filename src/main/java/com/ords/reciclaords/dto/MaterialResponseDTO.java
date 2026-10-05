package com.ords.reciclaords.dto;

import java.math.BigDecimal;

public record MaterialResponseDTO(
        Long id,
        String nome,
        String categoria,
        BigDecimal estoqueKg
) {
}