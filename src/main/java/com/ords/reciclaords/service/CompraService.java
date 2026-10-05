package com.ords.reciclaords.service;

import com.ords.reciclaords.dto.CompraRequestDTO;
import com.ords.reciclaords.dto.CompraResponseDTO;

import java.util.List;

public interface CompraService {

    CompraResponseDTO registrar(CompraRequestDTO dados);

    List<CompraResponseDTO> listarTodas();
}
