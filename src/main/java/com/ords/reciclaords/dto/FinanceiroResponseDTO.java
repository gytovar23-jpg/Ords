package com.ords.reciclaords.dto;

import java.math.BigDecimal;
import java.util.List;

public record FinanceiroResponseDTO(
        BigDecimal totalCompras,
        BigDecimal totalVendas,
        // totalVendas - totalCompras: positivo é lucro, negativo é prejuízo
        BigDecimal resultado,
        // Últimos 12 meses, do mais antigo para o atual (os totais acima são de todo o período)
        List<FinanceiroMesDTO> meses,
        List<FinanceiroMaterialDTO> materiais
) {
}
