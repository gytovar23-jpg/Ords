package com.ords.reciclaords.controller;

import com.ords.reciclaords.dto.MaterialRequestDTO;
import com.ords.reciclaords.dto.MaterialResponseDTO;
import com.ords.reciclaords.service.MaterialService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/materiais")
public class MaterialController {

    private final MaterialService materialService;

    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MaterialResponseDTO criar(@RequestBody @Valid MaterialRequestDTO dados) {
        return materialService.criar(dados);
    }

    @GetMapping
    public List<MaterialResponseDTO> listarTodos() {
        return materialService.listarTodos();
    }

    @GetMapping("/{id}")
    public MaterialResponseDTO buscarPorId(@PathVariable Long id) {
        return materialService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public MaterialResponseDTO atualizar(@PathVariable Long id, @RequestBody @Valid MaterialRequestDTO dados) {
        return materialService.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        materialService.excluir(id);
    }
}
