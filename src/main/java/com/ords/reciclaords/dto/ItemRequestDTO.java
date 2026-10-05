package com.ords.reciclaords.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

// Item de uma compra ou de uma venda
public record ItemRequestDTO(
        @NotNull Long materialId,
        @NotNull @Positive BigDecimal pesoKg,
        @NotNull @PositiveOrZero BigDecimal precoKg
) {}
