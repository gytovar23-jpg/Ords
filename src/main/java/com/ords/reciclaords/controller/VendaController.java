package com.ords.reciclaords.controller;

import com.ords.reciclaords.dto.VendaRequestDTO;
import com.ords.reciclaords.dto.VendaResponseDTO;
import com.ords.reciclaords.service.VendaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendas")
public class VendaController {

    private final VendaService vendaService;

    public VendaController(VendaService vendaService) {
        this.vendaService = vendaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VendaResponseDTO registrar(@RequestBody @Valid VendaRequestDTO dados) {
        return vendaService.registrar(dados);
    }

    @GetMapping
    public List<VendaResponseDTO> listarTodas() {
        return vendaService.listarTodas();
    }

    @PutMapping("/{id}")
    public VendaResponseDTO atualizar(@PathVariable Long id, @RequestBody @Valid VendaRequestDTO dados) {
        return vendaService.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        vendaService.excluir(id);
    }
}
