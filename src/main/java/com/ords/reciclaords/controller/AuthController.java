package com.ords.reciclaords.controller;

import com.ords.reciclaords.dto.AuthStatusDTO;
import com.ords.reciclaords.dto.CadastroRequestDTO;
import com.ords.reciclaords.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

// Login e logout são atendidos pelo Spring Security (ver SecurityConfig)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/status")
    public AuthStatusDTO status(Principal usuario) {
        return new AuthStatusDTO(usuarioService.existeDono(), usuario != null, usuario == null ? null : usuario.getName());
    }

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public void cadastrar(@RequestBody @Valid CadastroRequestDTO dados) {
        usuarioService.cadastrarDono(dados);
    }
}
