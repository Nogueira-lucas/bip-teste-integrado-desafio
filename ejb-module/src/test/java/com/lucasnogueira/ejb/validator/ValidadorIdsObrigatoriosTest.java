package com.lucasnogueira.ejb.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ValidadorIdsObrigatorios")
class ValidadorIdsObrigatoriosTest {

    private ValidadorIdsObrigatorios validador;

    @BeforeEach
    void setUp() {
        validador = new ValidadorIdsObrigatorios();
    }

    // -------------------------------------------------------------------------
    // Caminho feliz
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deve passar quando ambos os IDs sao validos")
    void devePassar_quandoAmbosIdsPresentes() {
        assertDoesNotThrow(() -> validador.validar(1L, 2L, BigDecimal.ONE, null, null));
    }

    @Test
    @DisplayName("deve passar quando IDs sao iguais (outra regra verifica isso)")
    void devePassar_quandoIdsSaoIguais() {
        assertDoesNotThrow(() -> validador.validar(1L, 1L, BigDecimal.ONE, null, null));
    }

    // -------------------------------------------------------------------------
    // Caminho de erro
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deve lancar IllegalArgumentException quando fromId e null")
    void deveLancar_quandoFromIdNulo() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validador.validar(null, 2L, BigDecimal.ONE, null, null));

        assertEquals("IDs de origem e destino são obrigatórios", ex.getMessage());
    }

    @Test
    @DisplayName("deve lancar IllegalArgumentException quando toId e null")
    void deveLancar_quandoToIdNulo() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validador.validar(1L, null, BigDecimal.ONE, null, null));

        assertEquals("IDs de origem e destino são obrigatórios", ex.getMessage());
    }

    @Test
    @DisplayName("deve lancar IllegalArgumentException quando ambos os IDs sao null")
    void deveLancar_quandoAmbosIdsNulos() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validador.validar(null, null, BigDecimal.ONE, null, null));

        assertEquals("IDs de origem e destino são obrigatórios", ex.getMessage());
    }
}
