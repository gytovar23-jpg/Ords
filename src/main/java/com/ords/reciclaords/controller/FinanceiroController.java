package com.ords.reciclaords.controller;

import com.ords.reciclaords.dto.FinanceiroResponseDTO;
import com.ords.reciclaords.service.FinanceiroService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/financeiro")
public class FinanceiroController {

    private final FinanceiroService financeiroService;

    public FinanceiroController(FinanceiroService financeiroService) {
        this.financeiroService = financeiroService;
    }

    @GetMapping
    public FinanceiroResponseDTO resumir() {
        return financeiroService.resumir();
    }
}
