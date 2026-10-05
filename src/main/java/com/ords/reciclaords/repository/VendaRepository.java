package com.ords.reciclaords.repository;

import com.ords.reciclaords.domain.Venda;
import com.ords.reciclaords.dto.ResumoMaterialDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendaRepository extends JpaRepository<Venda, Long> {

    List<Venda> findAllByOrderByDataDesc();

    boolean existsByItensMaterialId(Long materialId);

    @Query("""
            select new com.ords.reciclaords.dto.ResumoMaterialDTO(i.material.id, sum(i.pesoKg), sum(i.valorTotal))
            from VendaItem i
            group by i.material.id
            """)
    List<ResumoMaterialDTO> resumirPorMaterial();
}
