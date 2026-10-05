package com.ords.reciclaords.service;

import com.ords.reciclaords.domain.Material;
import com.ords.reciclaords.domain.Venda;
import com.ords.reciclaords.domain.VendaItem;
import com.ords.reciclaords.dto.ItemRequestDTO;
import com.ords.reciclaords.dto.ItemResponseDTO;
import com.ords.reciclaords.dto.VendaRequestDTO;
import com.ords.reciclaords.dto.VendaResponseDTO;
import com.ords.reciclaords.repository.MaterialRepository;
import com.ords.reciclaords.repository.VendaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VendaServiceImpl implements VendaService {

    private final VendaRepository vendaRepository;
    private final MaterialRepository materialRepository;

    public VendaServiceImpl(VendaRepository vendaRepository, MaterialRepository materialRepository) {
        this.vendaRepository = vendaRepository;
        this.materialRepository = materialRepository;
    }

    @Override
    @Transactional
    public VendaResponseDTO registrar(VendaRequestDTO dados) {

        Venda venda = Venda.builder()
                .comprador(dados.comprador() == null || dados.comprador().isBlank() ? null : dados.comprador().trim())
                .data(LocalDateTime.now())
                .valorTotal(BigDecimal.ZERO)
                .build();

        for (ItemRequestDTO item : dados.itens()) {

            Material material = materialRepository.findByIdParaAtualizar(item.materialId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Material não encontrado"));

            // Arredonda para a mesma precisão das colunas, para o total bater com o que fica gravado
            BigDecimal pesoKg = item.pesoKg().setScale(3, RoundingMode.HALF_UP);
            BigDecimal precoKg = item.precoKg().setScale(2, RoundingMode.HALF_UP);

            if (material.getEstoqueKg().compareTo(pesoKg) < 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Estoque insuficiente de " + material.getNome()
                                + ": disponível " + material.getEstoqueKg().toPlainString() + " kg");
            }

            BigDecimal valorItem = pesoKg.multiply(precoKg).setScale(2, RoundingMode.HALF_UP);

            venda.getItens().add(VendaItem.builder()
                    .venda(venda)
                    .material(material)
                    .pesoKg(pesoKg)
                    .precoKg(precoKg)
                    .valorTotal(valorItem)
                    .build());

            venda.setValorTotal(venda.getValorTotal().add(valorItem));

            // Saída de estoque
            material.setEstoqueKg(material.getEstoqueKg().subtract(pesoKg));
        }

        return converterParaResponse(vendaRepository.save(venda));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VendaResponseDTO> listarTodas() {

        return vendaRepository.findAllByOrderByDataDesc()
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    private VendaResponseDTO converterParaResponse(Venda venda) {

        List<ItemResponseDTO> itens = venda.getItens()
                .stream()
                .map(item -> new ItemResponseDTO(
                        item.getMaterial().getId(),
                        item.getMaterial().getNome(),
                        item.getPesoKg(),
                        item.getPrecoKg(),
                        item.getValorTotal()))
                .toList();

        return new VendaResponseDTO(
                venda.getId(),
                venda.getComprador(),
                venda.getData(),
                venda.getValorTotal(),
                itens
        );
    }
}
