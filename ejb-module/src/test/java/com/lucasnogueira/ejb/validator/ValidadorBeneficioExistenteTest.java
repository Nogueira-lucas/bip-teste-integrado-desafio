package com.lucasnogueira.ejb.validator;

import com.lucasnogueira.ejb.Beneficio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ValidadorBeneficioExistente")
class ValidadorBeneficioExistenteTest {

    private ValidadorBeneficioExistente validador;
    private Beneficio from;
    private Beneficio to;

    @BeforeEach
    void setUp() {
        validador = new ValidadorBeneficioExistente();

        from = new Beneficio();
        from.setId(1L);
        from.setValor(new BigDecimal("500.00"));

        to = new Beneficio();
        to.setId(2L);
        to.setValor(new BigDecimal("200.00"));
    }

    // -------------------------------------------------------------------------
    // Caminho feliz
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deve passar quando ambos os beneficios existem")
    void devePassar_quandoAmbosExistem() {
        assertDoesNotThrow(() -> validador.validar(1L, 2L, BigDecimal.ONE, from, to));
    }

    // -------------------------------------------------------------------------
    // Caminho de erro — origem
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deve lancar IllegalArgumentException quando beneficio de origem e null")
    void deveLancar_quandoFromNulo() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validador.validar(1L, 2L, BigDecimal.ONE, null, to));

        assertEquals("Benefício de origem não encontrado", ex.getMessage());
    }

    @Test
    @DisplayName("deve lancar excecao de origem antes de verificar destino quando ambos sao null")
    void deveLancarOrigem_quandoAmbosNulos() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validador.validar(1L, 2L, BigDecimal.ONE, null, null));

        assertEquals("Benefício de origem não encontrado", ex.getMessage());
    }

    // -------------------------------------------------------------------------
    // Caminho de erro — destino
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deve lancar IllegalArgumentException quando beneficio de destino e null")
    void deveLancar_quandoToNulo() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validador.validar(1L, 2L, BigDecimal.ONE, from, null));

        assertEquals("Benefício de destino não encontrado", ex.getMessage());
    }
}
