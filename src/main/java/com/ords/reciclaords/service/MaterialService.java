package com.ords.reciclaords.service;

import com.ords.reciclaords.domain.Material;
import com.ords.reciclaords.dto.MaterialRequestDTO;
import com.ords.reciclaords.dto.MaterialResponseDTO;

import java.util.List;

public interface MaterialService {

    MaterialResponseDTO criar(MaterialRequestDTO dados);

    MaterialResponseDTO buscarPorId(Long id);

    List<MaterialResponseDTO> listarTodos();

    MaterialResponseDTO atualizar(Long id, MaterialRequestDTO dados);

    void excluir(Long id);
}
