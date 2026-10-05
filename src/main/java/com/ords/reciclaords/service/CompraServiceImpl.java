package com.ords.reciclaords.service;

import com.ords.reciclaords.domain.Cliente;
import com.ords.reciclaords.domain.Compra;
import com.ords.reciclaords.domain.CompraItem;
import com.ords.reciclaords.domain.Material;
import com.ords.reciclaords.dto.CompraRequestDTO;
import com.ords.reciclaords.dto.CompraResponseDTO;
import com.ords.reciclaords.dto.ItemRequestDTO;
import com.ords.reciclaords.dto.ItemResponseDTO;
import com.ords.reciclaords.repository.ClienteRepository;
import com.ords.reciclaords.repository.CompraRepository;
import com.ords.reciclaords.repository.MaterialRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CompraServiceImpl implements CompraService {

    private final CompraRepository compraRepository;
    private final ClienteRepository clienteRepository;
    private final MaterialRepository materialRepository;

    public CompraServiceImpl(CompraRepository compraRepository,
                             ClienteRepository clienteRepository,
                             MaterialRepository materialRepository) {
        this.compraRepository = compraRepository;
        this.clienteRepository = clienteRepository;
        this.materialRepository = materialRepository;
    }

    @Override
    @Transactional
    public CompraResponseDTO registrar(CompraRequestDTO dados) {

        Cliente cliente = clienteRepository.findById(dados.clienteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));

        Compra compra = Compra.builder()
                .cliente(cliente)
                .data(LocalDateTime.now())
                .valorTotal(BigDecimal.ZERO)
                .build();

        for (ItemRequestDTO item : dados.itens()) {

            Material material = materialRepository.findByIdParaAtualizar(item.materialId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Material não encontrado"));

            // Arredonda para a mesma precisão das colunas, para o total bater com o que fica gravado
            BigDecimal pesoKg = item.pesoKg().setScale(3, RoundingMode.HALF_UP);
            BigDecimal precoKg = item.precoKg().setScale(2, RoundingMode.HALF_UP);
            BigDecimal valorItem = pesoKg.multiply(precoKg).setScale(2, RoundingMode.HALF_UP);

            compra.getItens().add(CompraItem.builder()
                    .compra(compra)
                    .material(material)
                    .pesoKg(pesoKg)
                    .precoKg(precoKg)
                    .valorTotal(valorItem)
                    .build());

            compra.setValorTotal(compra.getValorTotal().add(valorItem));

            // Entrada de estoque
            material.setEstoqueKg(material.getEstoqueKg().add(pesoKg));
        }

        return converterParaResponse(compraRepository.save(compra));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompraResponseDTO> listarTodas() {

        return compraRepository.findAllByOrderByDataDesc()
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    private CompraResponseDTO converterParaResponse(Compra compra) {

        List<ItemResponseDTO> itens = compra.getItens()
                .stream()
                .map(item -> new ItemResponseDTO(
                        item.getMaterial().getId(),
                        item.getMaterial().getNome(),
                        item.getPesoKg(),
                        item.getPrecoKg(),
                        item.getValorTotal()))
                .toList();

        return new CompraResponseDTO(
                compra.getId(),
                compra.getCliente().getId(),
                compra.getCliente().getNome(),
                compra.getData(),
                compra.getValorTotal(),
                itens
        );
    }
}
