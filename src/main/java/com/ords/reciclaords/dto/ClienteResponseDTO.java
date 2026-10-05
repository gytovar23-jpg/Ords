package com.ords.reciclaords.dto;

public record ClienteResponseDTO(
        Long id,
        String nome,
        String documento,
        String telefone
) {
}
