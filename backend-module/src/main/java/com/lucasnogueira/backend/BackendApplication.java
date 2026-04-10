package com.lucasnogueira.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// A entidade Beneficio está em com.lucasnogueira.ejb (ejb-module), fora do scan padrão.
// @EntityScan aponta o Hibernate para o pacote correto.
// @EnableJpaRepositories restringe o scan de repositórios ao pacote deste módulo.
@SpringBootApplication
@EntityScan("com.lucasnogueira.ejb")
@EnableJpaRepositories("com.lucasnogueira.backend.repository")
public class BackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
