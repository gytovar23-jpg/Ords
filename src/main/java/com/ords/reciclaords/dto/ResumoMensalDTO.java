package com.ords.reciclaords.dto;

import java.math.BigDecimal;

// Valor total das compras ou das vendas de um mês
public record ResumoMensalDTO(
        Integer ano,
        Integer mes,
        BigDecimal valor
) {
}
