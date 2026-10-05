package com.ords.reciclaords.service;

import com.ords.reciclaords.dto.FinanceiroMaterialDTO;
import com.ords.reciclaords.dto.FinanceiroMesDTO;
import com.ords.reciclaords.dto.FinanceiroResponseDTO;
import com.ords.reciclaords.dto.ResumoMaterialDTO;
import com.ords.reciclaords.dto.ResumoMensalDTO;
import com.ords.reciclaords.repository.CompraRepository;
import com.ords.reciclaords.repository.MaterialRepository;
import com.ords.reciclaords.repository.VendaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class FinanceiroServiceImpl implements FinanceiroService {

    private static final int MESES_DO_GRAFICO = 12;

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

        return new FinanceiroResponseDTO(totalCompras, totalVendas, totalVendas.subtract(totalCompras), resumirMeses(), materiais);
    }

    private List<FinanceiroMesDTO> resumirMeses() {

        YearMonth primeiro = YearMonth.now().minusMonths(MESES_DO_GRAFICO - 1);

        Map<YearMonth, BigDecimal> compras = indexarMeses(compraRepository.resumirPorMes(primeiro.atDay(1).atStartOfDay()));
        Map<YearMonth, BigDecimal> vendas = indexarMeses(vendaRepository.resumirPorMes(primeiro.atDay(1).atStartOfDay()));

        // Meses sem movimento entram zerados, para o gráfico não pular períodos
        List<FinanceiroMesDTO> meses = new ArrayList<>();
        for (int i = 0; i < MESES_DO_GRAFICO; i++) {
            YearMonth mes = primeiro.plusMonths(i);
            meses.add(new FinanceiroMesDTO(
                    mes.toString(),
                    compras.getOrDefault(mes, BigDecimal.ZERO),
                    vendas.getOrDefault(mes, BigDecimal.ZERO)));
        }
        return meses;
    }

    private Map<YearMonth, BigDecimal> indexarMeses(List<ResumoMensalDTO> resumos) {

        return resumos.stream().collect(Collectors.toMap(r -> YearMonth.of(r.ano(), r.mes()), ResumoMensalDTO::valor));
    }

    private Map<Long, ResumoMaterialDTO> indexar(List<ResumoMaterialDTO> resumos) {

        return resumos.stream().collect(Collectors.toMap(ResumoMaterialDTO::materialId, Function.identity()));
    }

    private BigDecimal somar(Map<Long, ResumoMaterialDTO> resumos) {

        return resumos.values().stream().map(ResumoMaterialDTO::valor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
