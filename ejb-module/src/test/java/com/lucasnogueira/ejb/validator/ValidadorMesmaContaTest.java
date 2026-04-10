package com.lucasnogueira.ejb.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ValidadorMesmaConta")
class ValidadorMesmaContaTest {

    private ValidadorMesmaConta validador;

    @BeforeEach
    void setUp() {
        validador = new ValidadorMesmaConta();
    }

    // -------------------------------------------------------------------------
    // Caminho feliz
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deve passar quando IDs sao diferentes")
    void devePassar_quandoIdsDiferentes() {
        assertDoesNotThrow(() -> validador.validar(1L, 2L, BigDecimal.ONE, null, null));
    }

    @Test
    @DisplayName("deve passar quando fromId e null (condicao requer fromId nao nulo)")
    void devePassar_quandoFromIdNulo() {
        assertDoesNotThrow(() -> validador.validar(null, 1L, BigDecimal.ONE, null, null));
    }

    @Test
    @DisplayName("deve passar quando ambos os IDs sao null")
    void devePassar_quandoAmbosNulos() {
        assertDoesNotThrow(() -> validador.validar(null, null, BigDecimal.ONE, null, null));
    }

    // -------------------------------------------------------------------------
    // Caminho de erro
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deve lancar IllegalArgumentException quando fromId e toId sao iguais")
    void deveLancar_quandoIdsSaoIguais() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validador.validar(1L, 1L, BigDecimal.ONE, null, null));

        assertEquals("Não é possível transferir para a mesma conta", ex.getMessage());
    }

    @Test
    @DisplayName("deve lancar IllegalArgumentException com qualquer valor de ID duplicado")
    void deveLancar_quandoQualquerIdDuplicado() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validador.validar(99L, 99L, BigDecimal.ONE, null, null));

        assertEquals("Não é possível transferir para a mesma conta", ex.getMessage());
    }
}
