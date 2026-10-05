package com.ords.reciclaords.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record VendaResponseDTO(
        Long id,
        String comprador,
        LocalDateTime data,
        BigDecimal valorTotal,
        List<ItemResponseDTO> itens
) {
}
