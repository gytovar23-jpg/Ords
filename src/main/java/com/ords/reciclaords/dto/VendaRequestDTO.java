package com.ords.reciclaords.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record VendaRequestDTO(
        String comprador,
        @NotEmpty @Valid List<ItemRequestDTO> itens
) {}
