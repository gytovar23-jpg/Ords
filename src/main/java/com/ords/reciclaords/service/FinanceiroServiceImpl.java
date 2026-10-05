package com.ords.reciclaords.service;

import com.ords.reciclaords.dto.FinanceiroMaterialDTO;
import com.ords.reciclaords.dto.FinanceiroResponseDTO;
import com.ords.reciclaords.dto.ResumoMaterialDTO;
import com.ords.reciclaords.repository.CompraRepository;
import com.ords.reciclaords.repository.MaterialRepository;
import com.ords.reciclaords.repository.VendaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class FinanceiroServiceImpl implements FinanceiroService {

    private static final ResumoMaterialDTO SEM_MOVIMENTO = new ResumoMaterialDTO(null, BigDecimal.ZERO, BigDecimal.ZERO);

    private final CompraRepository compraRepository;
    private final VendaRepository vendaRepository;
    private final MaterialRepository materialRepository;

    public FinanceiroServiceImpl(CompraRepository compraRepository,
                                 VendaRepository vendaRepository,
                                 MaterialRepository materialRepository) {
        this.compraRepository = compraRepository;
        this.vendaRepository = vendaRepository;
        this.materialRepository = materialRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public FinanceiroResponseDTO resumir() {

        Map<Long, ResumoMaterialDTO> compras = indexar(compraRepository.resumirPorMaterial());
        Map<Long, ResumoMaterialDTO> vendas = indexar(vendaRepository.resumirPorMaterial());

        List<FinanceiroMaterialDTO> materiais = materialRepository.findAll(Sort.by("nome"))
                .stream()
                .map(material -> {
                    ResumoMaterialDTO comprado = compras.getOrDefault(material.getId(), SEM_MOVIMENTO);
                    ResumoMaterialDTO vendido = vendas.getOrDefault(material.getId(), SEM_MOVIMENTO);
                    return new FinanceiroMaterialDTO(
                            material.getId(),
                            material.getNome(),
                            comprado.pesoKg(),
                            comprado.valor(),
                            vendido.pesoKg(),
                            vendido.valor(),
                            material.getEstoqueKg());
                })
                .toList();

        BigDecimal totalCompras = somar(compras);
        BigDecimal totalVendas = somar(vendas);

        return new FinanceiroResponseDTO(totalCompras, totalVendas, totalVendas.subtract(totalCompras), materiais);
    }

    private Map<Long, ResumoMaterialDTO> indexar(List<ResumoMaterialDTO> resumos) {

        return resumos.stream().collect(Collectors.toMap(ResumoMaterialDTO::materialId, Function.identity()));
    }

    private BigDecimal somar(Map<Long, ResumoMaterialDTO> resumos) {

        return resumos.values().stream().map(ResumoMaterialDTO::valor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
