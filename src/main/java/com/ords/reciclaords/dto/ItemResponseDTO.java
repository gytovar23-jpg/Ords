package com.ords.reciclaords.dto;

import java.math.BigDecimal;

public record ItemResponseDTO(
        Long materialId,
        String materialNome,
        BigDecimal pesoKg,
        BigDecimal precoKg,
        BigDecimal valorTotal
) {
}
