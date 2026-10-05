package com.ords.reciclaords.dto;

import jakarta.validation.constraints.NotBlank;

public record ClienteRequestDTO(
        @NotBlank String nome,
        String documento,
        String telefone
) {}
