package com.ords.reciclaords.domain;

import jakarta.persistence.*;
import lombok.*;

// Dono do site: a única pessoa que entra no sistema
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String login;

    // Hash BCrypt, nunca a senha em texto
    @Column(nullable = false)
    private String senha;
}
