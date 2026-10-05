package com.ords.reciclaords.service;

import com.ords.reciclaords.dto.CadastroRequestDTO;

public interface UsuarioService {

    boolean existeDono();

    void cadastrarDono(CadastroRequestDTO dados);
}
