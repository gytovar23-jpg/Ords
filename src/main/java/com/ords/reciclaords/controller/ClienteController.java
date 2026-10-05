package com.ords.reciclaords.controller;

import com.ords.reciclaords.dto.ClienteRequestDTO;
import com.ords.reciclaords.dto.ClienteResponseDTO;
import com.ords.reciclaords.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponseDTO criar(@RequestBody @Valid ClienteRequestDTO dados) {
        return clienteService.criar(dados);
    }

    @GetMapping
    public List<ClienteResponseDTO> listarTodos() {
        return clienteService.listarTodos();
    }

    @GetMapping("/{id}")
    public ClienteResponseDTO buscarPorId(@PathVariable Long id) {
        return clienteService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public ClienteResponseDTO atualizar(@PathVariable Long id, @RequestBody @Valid ClienteRequestDTO dados) {
        return clienteService.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        clienteService.excluir(id);
    }
}
