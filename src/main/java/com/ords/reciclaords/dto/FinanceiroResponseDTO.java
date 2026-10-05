package com.ords.reciclaords.dto;

import java.math.BigDecimal;
import java.util.List;

public record FinanceiroResponseDTO(
        BigDecimal totalCompras,
        BigDecimal totalVendas,
        // totalVendas - totalCompras: positivo é lucro, negativo é prejuízo
        BigDecimal resultado,
        List<FinanceiroMaterialDTO> materiais
) {
}
