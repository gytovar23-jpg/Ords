package com.ords.reciclaords.dto;

public record AuthStatusDTO(
        // Já existe um dono cadastrado? Se não, a tela de entrada mostra o cadastro
        boolean cadastrado,
        boolean autenticado,
        String login
) {
}
