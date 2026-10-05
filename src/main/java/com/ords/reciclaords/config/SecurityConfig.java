package com.ords.reciclaords.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth
                        // Só a tela de entrada fica aberta; todo o resto exige login
                        .requestMatchers("/login.html", "/login.js", "/style.css", "/logo.png", "/error").permitAll()
                        .requestMatchers("/api/auth/status", "/api/auth/cadastro").permitAll()
                        .anyRequest().authenticated())
                // As telas chamam a API por fetch, então login e logout respondem com status em vez de redirecionar
                .formLogin(form -> form
                        .loginPage("/login.html")
                        .loginProcessingUrl("/api/auth/login")
                        .usernameParameter("login")
                        .passwordParameter("senha")
                        .successHandler((request, response, authentication) ->
                                response.setStatus(HttpStatus.NO_CONTENT.value()))
                        .failureHandler((request, response, exception) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"message\":\"Usuário ou senha incorretos\"}");
                        }))
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT)))
                // Sem login: a API devolve 401 (a tela trata) e as páginas vão para o login
                .exceptionHandling(erros -> erros
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                PathPatternRequestMatcher.withDefaults().matcher("/api/**"))
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login.html"),
                                AnyRequestMatcher.INSTANCE))
                // Token CSRF em cookie, que o JavaScript devolve no cabeçalho X-XSRF-TOKEN
                .csrf(csrf -> csrf.spa());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
