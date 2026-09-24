package com.ords.reciclaords.repository;

import java.util.Optional;
import com.ords.reciclaords.domain.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {


    Optional<Material> findByNomeIgnoreCase(String nome);
}
