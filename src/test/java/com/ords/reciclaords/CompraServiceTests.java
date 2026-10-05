package com.ords.reciclaords;

import com.ords.reciclaords.domain.Cliente;
import com.ords.reciclaords.domain.Material;
import com.ords.reciclaords.dto.CompraRequestDTO;
import com.ords.reciclaords.dto.CompraResponseDTO;
import com.ords.reciclaords.dto.ItemRequestDTO;
import com.ords.reciclaords.dto.VendaRequestDTO;
import com.ords.reciclaords.repository.ClienteRepository;
import com.ords.reciclaords.repository.CompraRepository;
import com.ords.reciclaords.repository.MaterialRepository;
import com.ords.reciclaords.service.CompraService;
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
class CompraServiceTests {

    @Autowired
    private CompraService compraService;

    @Autowired
    private VendaService vendaService;

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    private Material material;
    private Cliente cliente;

    @BeforeEach
    void criarMaterialECliente() {
        material = materialRepository.save(Material.builder()
                .nome("Teste " + UUID.randomUUID())
                .categoria("Teste")
                .estoqueKg(BigDecimal.ZERO)
                .build());
        cliente = clienteRepository.save(Cliente.builder().nome("Cliente de teste").build());
    }

    @Test
    void alterarCompraRecalculaValorEAjustaEstoque() {
        CompraResponseDTO compra = compraService.registrar(compra("100", "2.00"));
        assertThat(estoque()).isEqualByComparingTo("100");

        CompraResponseDTO alterada = compraService.atualizar(compra.id(), compra("80", "3.00"));
        // Força o SQL (troca dos itens) a rodar no banco ainda dentro do teste
        compraRepository.flush();

        assertThat(alterada.id()).isEqualTo(compra.id());
        assertThat(alterada.data()).isEqualTo(compra.data());
        assertThat(alterada.valorTotal()).isEqualByComparingTo("240.00");
        assertThat(alterada.itens()).hasSize(1);
        assertThat(estoque()).isEqualByComparingTo("80");
    }

    @Test
    void alterarCompraParaMenosDoQueJaFoiVendidoERecusado() {
        CompraResponseDTO compra = compraService.registrar(compra("100", "2.00"));
        vender("70");

        // Sobram 30 kg; reduzir a compra para 60 deixaria o estoque em -10
        assertThatThrownBy(() -> compraService.atualizar(compra.id(), compra("60", "2.00")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("já foi vendida");
    }

    @Test
    void alterarCompraAteOLimiteDoQueJaFoiVendidoEAceito() {
        CompraResponseDTO compra = compraService.registrar(compra("100", "2.00"));
        vender("70");

        compraService.atualizar(compra.id(), compra("70", "2.00"));

        assertThat(estoque()).isEqualByComparingTo("0");
    }

    @Test
    void excluirCompraRetiraOEstoque() {
        CompraResponseDTO compra = compraService.registrar(compra("100", "2.00"));

        compraService.excluir(compra.id());
        compraRepository.flush();

        assertThat(estoque()).isEqualByComparingTo("0");
        assertThat(compraRepository.findById(compra.id())).isEmpty();
    }

    @Test
    void excluirCompraComMaterialJaVendidoERecusado() {
        CompraResponseDTO compra = compraService.registrar(compra("100", "2.00"));
        vender("70");

        assertThatThrownBy(() -> compraService.excluir(compra.id()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("já foi vendida");
    }

    private CompraRequestDTO compra(String pesoKg, String precoKg) {
        return new CompraRequestDTO(cliente.getId(),
                List.of(new ItemRequestDTO(material.getId(), new BigDecimal(pesoKg), new BigDecimal(precoKg))));
    }

    private void vender(String pesoKg) {
        vendaService.registrar(new VendaRequestDTO(null,
                List.of(new ItemRequestDTO(material.getId(), new BigDecimal(pesoKg), BigDecimal.ONE))));
    }

    private BigDecimal estoque() {
        return materialRepository.findById(material.getId()).orElseThrow().getEstoqueKg();
    }
}
