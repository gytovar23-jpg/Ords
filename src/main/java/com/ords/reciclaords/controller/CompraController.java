package com.ords.reciclaords.controller;

import com.ords.reciclaords.dto.CompraRequestDTO;
import com.ords.reciclaords.dto.CompraResponseDTO;
import com.ords.reciclaords.service.CompraService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/compras")
public class CompraController {

    private final CompraService compraService;

    public CompraController(CompraService compraService) {
        this.compraService = compraService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompraResponseDTO registrar(@RequestBody @Valid CompraRequestDTO dados) {
        return compraService.registrar(dados);
    }

    @GetMapping
    public List<CompraResponseDTO> listarTodas() {
        return compraService.listarTodas();
    }
}
