package com.ords.reciclaords.dto;

import java.math.BigDecimal;

public record MaterialRequestDTO(
        String nome,
        String categoria,
        BigDecimal precoPorKg,
        BigDecimal estoqueKg
) {}