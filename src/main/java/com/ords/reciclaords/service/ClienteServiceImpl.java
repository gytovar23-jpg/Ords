package com.ords.reciclaords.service;

import com.ords.reciclaords.domain.Cliente;
import com.ords.reciclaords.dto.ClienteRequestDTO;
import com.ords.reciclaords.dto.ClienteResponseDTO;
import com.ords.reciclaords.repository.ClienteRepository;
import com.ords.reciclaords.repository.CompraRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final CompraRepository compraRepository;

    public ClienteServiceImpl(ClienteRepository clienteRepository, CompraRepository compraRepository) {
        this.clienteRepository = clienteRepository;
        this.compraRepository = compraRepository;
    }

    @Override
    public ClienteResponseDTO criar(ClienteRequestDTO dados) {

        String documento = normalizar(dados.documento());
        validarDocumentoLivre(documento);

        Cliente cliente = Cliente.builder()
                .nome(dados.nome().trim())
                .documento(documento)
                .telefone(normalizar(dados.telefone()))
                .build();

        return converterParaResponse(clienteRepository.save(cliente));
    }

    @Override
    public ClienteResponseDTO buscarPorId(Long id) {

        return converterParaResponse(buscar(id));
    }

    @Override
    public List<ClienteResponseDTO> listarTodos() {

        return clienteRepository.findAll()
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Override
    public ClienteResponseDTO atualizar(Long id, ClienteRequestDTO dados) {

        Cliente cliente = buscar(id);

        String documento = normalizar(dados.documento());
        if (documento != null && !documento.equals(cliente.getDocumento())) {
            validarDocumentoLivre(documento);
        }

        cliente.setNome(dados.nome().trim());
        cliente.setDocumento(documento);
        cliente.setTelefone(normalizar(dados.telefone()));

        return converterParaResponse(clienteRepository.save(cliente));
    }

    @Override
    public void excluir(Long id) {

        Cliente cliente = buscar(id);

        if (compraRepository.existsByClienteId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cliente possui compras registradas e não pode ser excluído");
        }

        clienteRepository.delete(cliente);
    }

    private Cliente buscar(Long id) {

        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));
    }

    private void validarDocumentoLivre(String documento) {

        if (documento != null && clienteRepository.existsByDocumento(documento)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um cliente com esse documento");
        }
    }

    // Campos opcionais em branco viram null, para não esbarrar na restrição de documento único
    private String normalizar(String valor) {

        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private ClienteResponseDTO converterParaResponse(Cliente cliente) {

        return new ClienteResponseDTO(
                cliente.getId(),
                cliente.getNome(),
                cliente.getDocumento(),
                cliente.getTelefone()
        );
    }
}
