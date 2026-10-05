package com.ords.reciclaords.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record MaterialRequestDTO(
        @NotBlank String nome,
        @NotBlank String categoria,
        @PositiveOrZero BigDecimal estoqueKg
) {}
