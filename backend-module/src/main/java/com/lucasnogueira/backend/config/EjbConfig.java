package com.lucasnogueira.backend.config;

import com.lucasnogueira.ejb.BeneficioEjbService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra os beans do ejb-module no contexto Spring.
 *
 * O Spring aplica automaticamente o PersistenceAnnotationBeanPostProcessor,
 * que detecta a anotação @PersistenceContext em BeneficioEjbService e injeta
 * o EntityManager gerenciado pelo Spring — o mesmo participará das transações
 * abertas via @Transactional nos services deste módulo.
 */
@Configuration
public class EjbConfig {

    @Bean
    public BeneficioEjbService beneficioEjbService() {
        return new BeneficioEjbService();
    }
}
