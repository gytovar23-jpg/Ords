package com.ords.reciclaords.repository;

import com.ords.reciclaords.domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByDocumento(String documento);
}
