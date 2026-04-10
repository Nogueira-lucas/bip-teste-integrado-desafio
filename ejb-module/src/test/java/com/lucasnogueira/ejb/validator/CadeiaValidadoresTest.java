package com.lucasnogueira.ejb.validator;

import com.lucasnogueira.ejb.Beneficio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CadeiaValidadores")
class CadeiaValidadoresTest {

    @Mock
    private ValidadorTransferencia validadorA;

    @Mock
    private ValidadorTransferencia validadorB;

    // -------------------------------------------------------------------------
    // executarValidacoes — cadeia vazia
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("cadeia vazia nao deve lancar excecao para nenhuma entrada")
    void cadeiaVazia_devePassarSempre() {
        CadeiaValidadores cadeia = new CadeiaValidadores();

        assertDoesNotThrow(() ->
                cadeia.executarValidacoes(null, null, null, null, null));
    }

    // -------------------------------------------------------------------------
    // executarValidacoes — caminho feliz
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deve chamar todos os validadores quando nenhum lanca excecao")
    void deveChamarTodosValidadores_quandoEntradaValida() {
        CadeiaValidadores cadeia = new CadeiaValidadores()
                .adicionarValidador(validadorA)
                .adicionarValidador(validadorB);

        cadeia.executarValidacoes(1L, 2L, BigDecimal.ONE, null, null);

        verify(validadorA).validar(1L, 2L, BigDecimal.ONE, null, null);
        verify(validadorB).validar(1L, 2L, BigDecimal.ONE, null, null);
    }

    @Test
    @DisplayName("adicionarValidador deve retornar a propria cadeia (fluent interface)")
    void adicionarValidador_deveRetornarMesmaCadeia() {
        CadeiaValidadores cadeia = new CadeiaValidadores();

        CadeiaValidadores retorno = cadeia.adicionarValidador(validadorA);

        assertSame(cadeia, retorno);
    }

    // -------------------------------------------------------------------------
    // executarValidacoes — propagacao de excecao
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deve propagar a excecao do primeiro validador que falhar")
    void devePropagar_excecaoDoValidadorQuefalhou() {
        doThrow(new IllegalArgumentException("falhou"))
                .when(validadorA).validar(any(), any(), any(), any(), any());

        CadeiaValidadores cadeia = new CadeiaValidadores()
                .adicionarValidador(validadorA)
                .adicionarValidador(validadorB);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> cadeia.executarValidacoes(1L, 2L, BigDecimal.ONE, null, null));

        assertEquals("falhou", ex.getMessage());
    }

    @Test
    @DisplayName("deve interromper a cadeia no primeiro validador que lancar excecao")
    void deveInterromper_aoChamarValidadorQuefalha() {
        doThrow(new IllegalArgumentException("validadorA falhou"))
                .when(validadorA).validar(any(), any(), any(), any(), any());

        CadeiaValidadores cadeia = new CadeiaValidadores()
                .adicionarValidador(validadorA)
                .adicionarValidador(validadorB);

        assertThrows(IllegalArgumentException.class,
                () -> cadeia.executarValidacoes(1L, 2L, BigDecimal.ONE, null, null));

        // validadorB nao deve ter sido chamado
        verify(validadorB, never()).validar(any(), any(), any(), any(), any());
    }

    // -------------------------------------------------------------------------
    // construirCadeiaCompleta — integracao dos 5 validadores
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("construirCadeiaCompleta deve rejeitar amount nulo (ValidadorValorPositivo)")
    void construirCadeiaCompleta_deveRejeitar_amountNulo() {
        CadeiaValidadores cadeia = CadeiaValidadores.construirCadeiaCompleta();

        assertThrows(IllegalArgumentException.class,
                () -> cadeia.executarValidacoes(1L, 2L, null, null, null));
    }

    @Test
    @DisplayName("construirCadeiaCompleta deve rejeitar fromId nulo (ValidadorIdsObrigatorios)")
    void construirCadeiaCompleta_deveRejeitar_fromIdNulo() {
        CadeiaValidadores cadeia = CadeiaValidadores.construirCadeiaCompleta();

        assertThrows(IllegalArgumentException.class,
                () -> cadeia.executarValidacoes(null, 2L, BigDecimal.ONE, null, null));
    }

    @Test
    @DisplayName("construirCadeiaCompleta deve rejeitar mesma conta (ValidadorMesmaConta)")
    void construirCadeiaCompleta_deveRejeitar_mesmaConta() {
        CadeiaValidadores cadeia = CadeiaValidadores.construirCadeiaCompleta();

        assertThrows(IllegalArgumentException.class,
                () -> cadeia.executarValidacoes(1L, 1L, BigDecimal.ONE, null, null));
    }

    @Test
    @DisplayName("construirCadeiaCompleta deve rejeitar beneficio de origem nulo (ValidadorBeneficioExistente)")
    void construirCadeiaCompleta_deveRejeitar_beneficioOrigemNulo() {
        CadeiaValidadores cadeia = CadeiaValidadores.construirCadeiaCompleta();

        Beneficio to = new Beneficio();
        to.setValor(BigDecimal.ONE);

        assertThrows(IllegalArgumentException.class,
                () -> cadeia.executarValidacoes(1L, 2L, BigDecimal.ONE, null, to));
    }

    @Test
    @DisplayName("construirCadeiaCompleta deve rejeitar saldo insuficiente (ValidadorSaldoSuficiente)")
    void construirCadeiaCompleta_deveRejeitar_saldoInsuficiente() {
        CadeiaValidadores cadeia = CadeiaValidadores.construirCadeiaCompleta();

        Beneficio from = new Beneficio();
        from.setValor(new BigDecimal("10.00"));

        Beneficio to = new Beneficio();
        to.setValor(BigDecimal.ONE);

        assertThrows(IllegalStateException.class,
                () -> cadeia.executarValidacoes(1L, 2L, new BigDecimal("11.00"), from, to));
    }

    @Test
    @DisplayName("construirCadeiaCompleta deve passar com entrada completamente valida")
    void construirCadeiaCompleta_devePassar_quandoEntradaValida() {
        CadeiaValidadores cadeia = CadeiaValidadores.construirCadeiaCompleta();

        Beneficio from = new Beneficio();
        from.setValor(new BigDecimal("500.00"));

        Beneficio to = new Beneficio();
        to.setValor(new BigDecimal("100.00"));

        assertDoesNotThrow(() ->
                cadeia.executarValidacoes(1L, 2L, new BigDecimal("300.00"), from, to));
    }
}
