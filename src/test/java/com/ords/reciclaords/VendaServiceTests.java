package com.ords.reciclaords;

import com.ords.reciclaords.domain.Material;
import com.ords.reciclaords.dto.ItemRequestDTO;
import com.ords.reciclaords.dto.VendaRequestDTO;
import com.ords.reciclaords.dto.VendaResponseDTO;
import com.ords.reciclaords.repository.MaterialRepository;
import com.ords.reciclaords.repository.VendaRepository;
import com.ords.reciclaords.service.VendaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Cada teste roda em uma transação que é desfeita no fim, então nada fica gravado no banco
@SpringBootTest
@Transactional
class VendaServiceTests {

    @Autowired
    private VendaService vendaService;

    @Autowired
    private VendaRepository vendaRepository;

    @Autowired
    private MaterialRepository materialRepository;

    private Material material;

    @BeforeEach
    void criarMaterialComEstoque() {
        material = materialRepository.save(Material.builder()
                .nome("Teste " + UUID.randomUUID())
                .categoria("Teste")
                .estoqueKg(new BigDecimal("100.000"))
                .build());
    }

    @Test
    void alterarVendaRecalculaValorEAjustaEstoque() {
        VendaResponseDTO venda = vendaService.registrar(venda("30", "2.00"));
        assertThat(estoque()).isEqualByComparingTo("70");

        VendaResponseDTO alterada = vendaService.atualizar(venda.id(), venda("50", "3.00"));
        // Força o SQL (troca dos itens) a rodar no banco ainda dentro do teste
        vendaRepository.flush();

        assertThat(alterada.id()).isEqualTo(venda.id());
        assertThat(alterada.data()).isEqualTo(venda.data());
        assertThat(alterada.valorTotal()).isEqualByComparingTo("150.00");
        assertThat(alterada.itens()).hasSize(1);
        assertThat(estoque()).isEqualByComparingTo("50");
    }

    @Test
    void alterarVendaPodeUsarOEstoqueQueElaMesmaDevolve() {
        VendaResponseDTO venda = vendaService.registrar(venda("90", "1.00"));

        // Só restam 10 kg, mas os 90 da própria venda voltam antes de sair os 100
        vendaService.atualizar(venda.id(), venda("100", "1.00"));

        assertThat(estoque()).isEqualByComparingTo("0");
    }

    @Test
    void alterarVendaAlemDoEstoqueERecusado() {
        VendaResponseDTO venda = vendaService.registrar(venda("30", "2.00"));

        assertThatThrownBy(() -> vendaService.atualizar(venda.id(), venda("130.001", "2.00")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Estoque insuficiente");
    }

    @Test
    void excluirVendaDevolveOEstoque() {
        VendaResponseDTO venda = vendaService.registrar(venda("30", "2.00"));

        vendaService.excluir(venda.id());
        vendaRepository.flush();

        assertThat(estoque()).isEqualByComparingTo("100");
        assertThat(vendaRepository.findById(venda.id())).isEmpty();
    }

    private VendaRequestDTO venda(String pesoKg, String precoKg) {
        return new VendaRequestDTO("Comprador de teste",
                List.of(new ItemRequestDTO(material.getId(), new BigDecimal(pesoKg), new BigDecimal(precoKg))));
    }

    private BigDecimal estoque() {
        return materialRepository.findById(material.getId()).orElseThrow().getEstoqueKg();
    }
}
