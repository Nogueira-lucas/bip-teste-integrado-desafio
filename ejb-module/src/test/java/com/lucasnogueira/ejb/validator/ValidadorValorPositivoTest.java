package com.lucasnogueira.ejb.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ValidadorValorPositivo")
class ValidadorValorPositivoTest {

    private ValidadorValorPositivo validador;

    @BeforeEach
    void setUp() {
        validador = new ValidadorValorPositivo();
    }

    // -------------------------------------------------------------------------
    // Caminho feliz
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deve passar com valor positivo inteiro")
    void devePassar_quandoValorPositivoInteiro() {
        assertDoesNotThrow(() -> validador.validar(1L, 2L, new BigDecimal("100"), null, null));
    }

    @Test
    @DisplayName("deve passar com valor positivo decimal")
    void devePassar_quandoValorPositivoDecimal() {
        assertDoesNotThrow(() -> validador.validar(1L, 2L, new BigDecimal("0.01"), null, null));
    }

    @Test
    @DisplayName("deve passar com valor muito grande")
    void devePassar_quandoValorGrande() {
        assertDoesNotThrow(() -> validador.validar(1L, 2L, new BigDecimal("999999999.99"), null, null));
    }

    // -------------------------------------------------------------------------
    // Caminho de erro
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deve lancar IllegalArgumentException quando amount e null")
    void deveLancar_quandoAmountNulo() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validador.validar(1L, 2L, null, null, null));

        assertEquals("Valor da transferência deve ser positivo", ex.getMessage());
    }

    @Test
    @DisplayName("deve lancar IllegalArgumentException quando amount e zero")
    void deveLancar_quandoAmountZero() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validador.validar(1L, 2L, BigDecimal.ZERO, null, null));

        assertEquals("Valor da transferência deve ser positivo", ex.getMessage());
    }

    @Test
    @DisplayName("deve lancar IllegalArgumentException quando amount e negativo")
    void deveLancar_quandoAmountNegativo() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validador.validar(1L, 2L, new BigDecimal("-0.01"), null, null));

        assertEquals("Valor da transferência deve ser positivo", ex.getMessage());
    }

    @Test
    @DisplayName("deve lancar IllegalArgumentException com valor muito negativo")
    void deveLancar_quandoAmountMuitoNegativo() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validador.validar(1L, 2L, new BigDecimal("-999999"), null, null));

        assertEquals("Valor da transferência deve ser positivo", ex.getMessage());
    }
}
