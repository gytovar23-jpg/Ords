package com.ords.reciclaords.repository;

import com.ords.reciclaords.domain.Compra;
import com.ords.reciclaords.dto.ResumoMaterialDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {

    List<Compra> findAllByOrderByDataDesc();

    boolean existsByClienteId(Long clienteId);

    boolean existsByItensMaterialId(Long materialId);

    @Query("""
            select new com.ords.reciclaords.dto.ResumoMaterialDTO(i.material.id, sum(i.pesoKg), sum(i.valorTotal))
            from CompraItem i
            group by i.material.id
            """)
    List<ResumoMaterialDTO> resumirPorMaterial();
}
