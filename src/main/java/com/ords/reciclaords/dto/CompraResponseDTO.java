package com.ords.reciclaords.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CompraResponseDTO(
        Long id,
        Long clienteId,
        String clienteNome,
        LocalDateTime data,
        BigDecimal valorTotal,
        List<ItemResponseDTO> itens
) {
}
