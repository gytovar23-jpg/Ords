package com.ords.reciclaords.repository;

import com.ords.reciclaords.domain.Compra;
import com.ords.reciclaords.dto.ResumoMaterialDTO;
import com.ords.reciclaords.dto.ResumoMensalDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    @Query("""
            select new com.ords.reciclaords.dto.ResumoMensalDTO(year(x.data), month(x.data), sum(x.valorTotal))
            from Compra x
            where x.data >= :inicio
            group by year(x.data), month(x.data)
            """)
    List<ResumoMensalDTO> resumirPorMes(LocalDateTime inicio);
}
