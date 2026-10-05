package com.ords.reciclaords.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastroRequestDTO(
        @NotBlank @Size(max = 60) String login,
        // 72 é o limite de caracteres que o BCrypt considera
        @NotBlank @Size(min = 8, max = 72) String senha
) {}
