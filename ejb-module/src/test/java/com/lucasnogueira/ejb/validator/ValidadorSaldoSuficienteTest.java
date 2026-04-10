package com.lucasnogueira.ejb.validator;

import com.lucasnogueira.ejb.Beneficio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ValidadorSaldoSuficiente")
class ValidadorSaldoSuficienteTest {

    private ValidadorSaldoSuficiente validador;
    private Beneficio from;

    @BeforeEach
    void setUp() {
        validador = new ValidadorSaldoSuficiente();

        from = new Beneficio();
        from.setId(1L);
        from.setValor(new BigDecimal("1000.00"));
    }

    // -------------------------------------------------------------------------
    // Caminho feliz
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deve passar quando saldo e maior que o amount")
    void devePassar_quandoSaldoMaiorQueAmount() {
        assertDoesNotThrow(() -> validador.validar(1L, 2L, new BigDecimal("999.99"), from, null));
    }

    @Test
    @DisplayName("deve passar quando saldo e exatamente igual ao amount (limite exato)")
    void devePassar_quandoSaldoIgualAoAmount() {
        // compareTo == 0, nao < 0 → passa
        assertDoesNotThrow(() -> validador.validar(1L, 2L, new BigDecimal("1000.00"), from, null));
    }

    @Test
    @DisplayName("deve passar quando from e null (sem entidade para verificar)")
    void devePassar_quandoFromNulo() {
        // condicao tem "from != null &&", entao null e ignorado
        assertDoesNotThrow(() -> validador.validar(1L, 2L, new BigDecimal("9999.00"), null, null));
    }

    @Test
    @DisplayName("deve passar quando saldo e zero e amount e zero (edge case de valor nao positivo)")
    void devePassar_quandoSaldoZeroEAmountZero() {
        from.setValor(BigDecimal.ZERO);
        // 0.compareTo(0) == 0, nao < 0 → passa
        assertDoesNotThrow(() -> validador.validar(1L, 2L, BigDecimal.ZERO, from, null));
    }

    // -------------------------------------------------------------------------
    // Caminho de erro
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deve lancar IllegalStateException quando saldo e insuficiente")
    void deveLancar_quandoSaldoInsuficiente() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> validador.validar(1L, 2L, new BigDecimal("1000.01"), from, null));

        assertEquals("Saldo insuficiente para transferência", ex.getMessage());
    }

    @Test
    @DisplayName("deve lancar IllegalStateException quando saldo e zero e amount e positivo")
    void deveLancar_quandoSaldoZeroEAmountPositivo() {
        from.setValor(BigDecimal.ZERO);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> validador.validar(1L, 2L, new BigDecimal("0.01"), from, null));

        assertEquals("Saldo insuficiente para transferência", ex.getMessage());
    }

    @Test
    @DisplayName("deve lancar IllegalStateException com saldo muito abaixo do amount")
    void deveLancar_quandoSaldoMuitoAbaixoDoAmount() {
        from.setValor(new BigDecimal("1.00"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> validador.validar(1L, 2L, new BigDecimal("999999.00"), from, null));

        assertEquals("Saldo insuficiente para transferência", ex.getMessage());
    }
}
