package com.ords.reciclaords.service;

import com.ords.reciclaords.domain.Material;
import com.ords.reciclaords.dto.MaterialRequestDTO;
import com.ords.reciclaords.dto.MaterialResponseDTO;
import com.ords.reciclaords.repository.CompraRepository;
import com.ords.reciclaords.repository.MaterialRepository;
import com.ords.reciclaords.repository.VendaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class MaterialServiceImpl implements MaterialService {

    private final MaterialRepository materialRepository;
    private final CompraRepository compraRepository;
    private final VendaRepository vendaRepository;

    public MaterialServiceImpl(MaterialRepository materialRepository,
                               CompraRepository compraRepository,
                               VendaRepository vendaRepository) {
        this.materialRepository = materialRepository;
        this.compraRepository = compraRepository;
        this.vendaRepository = vendaRepository;
    }

    @Override
    public MaterialResponseDTO criar(MaterialRequestDTO dados) {

        validarNomeLivre(dados.nome(), null);

        Material material = Material.builder()
                .nome(dados.nome().trim())
                .categoria(dados.categoria().trim())
                .estoqueKg(dados.estoqueKg() == null ? BigDecimal.ZERO : dados.estoqueKg())
                .build();

        Material materialSalvo = materialRepository.save(material);

        return converterParaResponse(materialSalvo);
    }

    @Override
    public MaterialResponseDTO buscarPorId(Long id) {

        return converterParaResponse(buscar(id));
    }

    @Override
    public List<MaterialResponseDTO> listarTodos() {

        return materialRepository.findAll(Sort.by("nome"))
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Override
    public MaterialResponseDTO atualizar(Long id, MaterialRequestDTO dados) {

        Material material = buscar(id);

        validarNomeLivre(dados.nome(), id);

        material.setNome(dados.nome().trim());
        material.setCategoria(dados.categoria().trim());
        // Sem estoque informado, mantém o atual (ele é movimentado pelas compras e vendas)
        if (dados.estoqueKg() != null) {
            material.setEstoqueKg(dados.estoqueKg());
        }

        Material materialAtualizado = materialRepository.save(material);

        return converterParaResponse(materialAtualizado);
    }

    @Override
    public void excluir(Long id) {

        Material material = buscar(id);

        if (compraRepository.existsByItensMaterialId(id) || vendaRepository.existsByItensMaterialId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Material possui compras ou vendas registradas e não pode ser excluído");
        }

        materialRepository.delete(material);
    }

    private Material buscar(Long id) {

        return materialRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Material não encontrado"));
    }

    private void validarNomeLivre(String nome, Long idAtual) {

        materialRepository.findByNomeIgnoreCase(nome.trim())
                .filter(existente -> !existente.getId().equals(idAtual))
                .ifPresent(existente -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um material com esse nome");
                });
    }

    private MaterialResponseDTO converterParaResponse(Material material) {

        return new MaterialResponseDTO(
                material.getId(),
                material.getNome(),
                material.getCategoria(),
                material.getEstoqueKg()
        );
    }
}
