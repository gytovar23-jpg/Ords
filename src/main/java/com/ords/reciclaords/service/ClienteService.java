package com.ords.reciclaords.service;

import com.ords.reciclaords.dto.ClienteRequestDTO;
import com.ords.reciclaords.dto.ClienteResponseDTO;

import java.util.List;

public interface ClienteService {

    ClienteResponseDTO criar(ClienteRequestDTO dados);

    ClienteResponseDTO buscarPorId(Long id);

    List<ClienteResponseDTO> listarTodos();

    ClienteResponseDTO atualizar(Long id, ClienteRequestDTO dados);

    void excluir(Long id);
}
