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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

        Compra compra = Compra.builder()
                .cliente(buscarCliente(dados.clienteId()))
                .data(LocalDateTime.now())
                .valorTotal(BigDecimal.ZERO)
                .build();

        lancarItens(compra, dados.itens());

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

    @Override
    @Transactional
    public CompraResponseDTO atualizar(Long id, CompraRequestDTO dados) {

        Compra compra = buscar(id);

        // Desfaz a compra antiga e lança a corrigida; o saldo só é conferido no fim, já com os novos itens.
        // A data original é mantida, para a compra continuar no mesmo mês do financeiro.
        Set<Material> movimentados = retirarEstoque(compra);
        compra.getItens().clear();
        compra.setValorTotal(BigDecimal.ZERO);
        compra.setCliente(buscarCliente(dados.clienteId()));

        movimentados.addAll(lancarItens(compra, dados.itens()));
        validarEstoque(movimentados);

        return converterParaResponse(compraRepository.save(compra));
    }

    @Override
    @Transactional
    public void excluir(Long id) {

        Compra compra = buscar(id);

        validarEstoque(retirarEstoque(compra));

        compraRepository.delete(compra);
    }

    private Compra buscar(Long id) {

        return compraRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Compra não encontrada"));
    }

    private Cliente buscarCliente(Long id) {

        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));
    }

    // O que entrou nessa compra sai do estoque
    private Set<Material> retirarEstoque(Compra compra) {

        Set<Material> movimentados = new LinkedHashSet<>();

        for (CompraItem item : compra.getItens()) {

            Material material = materialRepository.findByIdParaAtualizar(item.getMaterial().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Material não encontrado"));

            material.setEstoqueKg(material.getEstoqueKg().subtract(item.getPesoKg()));
            movimentados.add(material);
        }

        return movimentados;
    }

    // Material comprado que já foi vendido não pode sumir do estoque: o saldo ficaria negativo
    private void validarEstoque(Set<Material> materiais) {

        for (Material material : materiais) {
            if (material.getEstoqueKg().signum() < 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Parte do material " + material.getNome() + " dessa compra já foi vendida: faltariam "
                                + material.getEstoqueKg().negate().toPlainString() + " kg no estoque");
            }
        }
    }

    private Set<Material> lancarItens(Compra compra, List<ItemRequestDTO> itens) {

        Set<Material> movimentados = new LinkedHashSet<>();

        for (ItemRequestDTO item : itens) {

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
            movimentados.add(material);
        }

        return movimentados;
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
