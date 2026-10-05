package com.ords.reciclaords.service;

import com.ords.reciclaords.dto.VendaRequestDTO;
import com.ords.reciclaords.dto.VendaResponseDTO;

import java.util.List;

public interface VendaService {

    VendaResponseDTO registrar(VendaRequestDTO dados);

    List<VendaResponseDTO> listarTodas();
}
