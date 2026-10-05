package com.ords.reciclaords.repository;

import java.util.Optional;
import com.ords.reciclaords.domain.Material;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {


    Optional<Material> findByNomeIgnoreCase(String nome);

    // Trava a linha do material enquanto o estoque é movimentado, para duas operações simultâneas não se sobrescreverem
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Material m where m.id = :id")
    Optional<Material> findByIdParaAtualizar(Long id);
}
