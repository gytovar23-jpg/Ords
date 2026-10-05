package com.ords.reciclaords.dto;

import java.math.BigDecimal;

public record FinanceiroMaterialDTO(
        Long materialId,
        String materialNome,
        BigDecimal kgComprado,
        BigDecimal valorComprado,
        BigDecimal kgVendido,
        BigDecimal valorVendido,
        BigDecimal estoqueKg
) {
}
