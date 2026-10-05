package com.ords.reciclaords.dto;

import java.math.BigDecimal;

// Totais de peso e valor de um material, somados a partir dos itens de compra ou de venda
public record ResumoMaterialDTO(
        Long materialId,
        BigDecimal pesoKg,
        BigDecimal valor
) {
}
