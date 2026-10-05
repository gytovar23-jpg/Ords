package com.ords.reciclaords.dto;

import java.math.BigDecimal;

public record FinanceiroMesDTO(
        // Formato AAAA-MM
        String mes,
        BigDecimal compras,
        BigDecimal vendas
) {
}
