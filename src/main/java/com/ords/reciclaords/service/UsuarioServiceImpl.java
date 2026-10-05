package com.ords.reciclaords.service;

import com.ords.reciclaords.domain.Usuario;
import com.ords.reciclaords.dto.CadastroRequestDTO;
import com.ords.reciclaords.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioServiceImpl implements UsuarioService, UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean existeDono() {

        return usuarioRepository.count() > 0;
    }

    // synchronized: dois cadastros ao mesmo tempo não podem passar juntos pela checagem
    @Override
    public synchronized void cadastrarDono(CadastroRequestDTO dados) {

        // O cadastro só fica aberto enquanto não houver dono; depois disso, só entra quem tem a senha
        if (existeDono()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O dono do site já está cadastrado");
        }

        usuarioRepository.save(Usuario.builder()
                .login(dados.login().trim())
                .senha(passwordEncoder.encode(dados.senha()))
                .build());
    }

    @Override
    public UserDetails loadUserByUsername(String login) {

        Usuario usuario = usuarioRepository.findByLoginIgnoreCase(login.trim())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        return User.withUsername(usuario.getLogin())
                .password(usuario.getSenha())
                .roles("DONO")
                .build();
    }
}
