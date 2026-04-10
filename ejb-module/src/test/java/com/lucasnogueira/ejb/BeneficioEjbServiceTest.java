package com.lucasnogueira.ejb;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BeneficioEjbService")
class BeneficioEjbServiceTest {

    @Mock
    private EntityManager em;

    @InjectMocks
    private BeneficioEjbService service;

    private Beneficio origem;
    private Beneficio destino;

    @BeforeEach
    void setUp() {
        origem = new Beneficio();
        origem.setId(1L);
        origem.setNome("Beneficio Origem");
        origem.setValor(new BigDecimal("1000.00"));
        origem.setAtivo(true);

        destino = new Beneficio();
        destino.setId(2L);
        destino.setNome("Beneficio Destino");
        destino.setValor(new BigDecimal("500.00"));
        destino.setAtivo(true);
    }

    /** Stub padrão: em.find com PESSIMISTIC_WRITE para os ids 1L e 2L. */
    private void stubFind() {
        when(em.find(Beneficio.class, 1L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(origem);
        when(em.find(Beneficio.class, 2L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(destino);
    }

    // -------------------------------------------------------------------------
    // Caminho feliz
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("transfer deve subtrair valor da origem e adicionar ao destino")
    void transfer_deveAtualizarValoresCorretamente() {
        stubFind();

        service.transfer(1L, 2L, new BigDecimal("300.00"));

        assertEquals(new BigDecimal("700.00"), origem.getValor());
        assertEquals(new BigDecimal("800.00"), destino.getValor());
        verify(em).flush();
    }

    @Test
    @DisplayName("transfer deve chamar flush apos realizar a transferencia")
    void transfer_deveChamarFlushAposPersistir() {
        stubFind();

        service.transfer(1L, 2L, new BigDecimal("100.00"));

        verify(em, times(1)).flush();
    }

    @Test
    @DisplayName("transfer deve ter sucesso quando amount igual ao saldo total da origem")
    void transfer_devePermitir_quandoValorIgualAoSaldoTotal() {
        // origin=1000, amount=1000 → ValidadorSaldoSuficiente: 1000 < 1000 → false → passa
        stubFind();

        service.transfer(1L, 2L, new BigDecimal("1000.00"));

        assertEquals(new BigDecimal("0.00"), origem.getValor());
        assertEquals(new BigDecimal("1500.00"), destino.getValor());
        verify(em).flush();
    }

    @Test
    @DisplayName("transfer deve ter sucesso com valor menor que o saldo da origem")
    void transfer_devePermitir_quandoAmountMenorQueOSaldo() {
        // origin=1000, amount=500 → passa
        stubFind();

        service.transfer(1L, 2L, new BigDecimal("500.00"));

        assertEquals(new BigDecimal("500.00"), origem.getValor());
        assertEquals(new BigDecimal("1000.00"), destino.getValor());
    }

    // -------------------------------------------------------------------------
    // ValidadorSaldoSuficiente
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("transfer deve lancar IllegalStateException quando saldo insuficiente")
    void transfer_deveLancarExcecao_quandoSaldoInsuficiente() {
        // origin=1000, amount=1001 → 1000 < 1001 → IllegalStateException
        stubFind();

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.transfer(1L, 2L, new BigDecimal("1001.00")));

        assertEquals("Saldo insuficiente para transferência", ex.getMessage());
        verify(em, never()).flush();
    }

    // -------------------------------------------------------------------------
    // ValidadorValorPositivo
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("transfer deve lancar IllegalArgumentException quando valor e zero")
    void transfer_lancaExcecao_quandoValorZero() {
        stubFind();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.transfer(1L, 2L, BigDecimal.ZERO));

        assertEquals("Valor da transferência deve ser positivo", ex.getMessage());
        verify(em, never()).flush();
    }

    @Test
    @DisplayName("transfer deve lancar IllegalArgumentException quando valor e negativo")
    void transfer_lancaExcecao_quandoValorNegativo() {
        stubFind();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.transfer(1L, 2L, new BigDecimal("-50.00")));

        assertEquals("Valor da transferência deve ser positivo", ex.getMessage());
        verify(em, never()).flush();
    }

    // -------------------------------------------------------------------------
    // ValidadorMesmaConta
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("transfer deve lancar IllegalArgumentException quando fromId e toId sao iguais")
    void transfer_lancaExcecao_quandoMesmaConta() {
        // find e chamado antes dos validators; mesmo id retorna a mesma entidade
        when(em.find(Beneficio.class, 1L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(origem);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.transfer(1L, 1L, new BigDecimal("100.00")));

        assertEquals("Não é possível transferir para a mesma conta", ex.getMessage());
        verify(em, never()).flush();
    }

    // -------------------------------------------------------------------------
    // ValidadorBeneficioExistente
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("transfer deve lancar IllegalArgumentException quando beneficio de origem nao existe")
    void transfer_lancaExcecao_quandoOrigemNaoEncontrada() {
        when(em.find(Beneficio.class, 99L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(null);
        when(em.find(Beneficio.class, 2L,  LockModeType.PESSIMISTIC_WRITE)).thenReturn(destino);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.transfer(99L, 2L, new BigDecimal("100.00")));

        assertEquals("Benefício de origem não encontrado", ex.getMessage());
        verify(em, never()).flush();
    }

    @Test
    @DisplayName("transfer deve lancar IllegalArgumentException quando beneficio de destino nao existe")
    void transfer_lancaExcecao_quandoDestinoNaoEncontrado() {
        when(em.find(Beneficio.class, 1L,  LockModeType.PESSIMISTIC_WRITE)).thenReturn(origem);
        when(em.find(Beneficio.class, 99L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.transfer(1L, 99L, new BigDecimal("100.00")));

        assertEquals("Benefício de destino não encontrado", ex.getMessage());
        verify(em, never()).flush();
    }
}
