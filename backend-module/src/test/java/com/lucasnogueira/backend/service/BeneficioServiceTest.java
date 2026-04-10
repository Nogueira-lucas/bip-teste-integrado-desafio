package com.lucasnogueira.backend.service;

import com.lucasnogueira.backend.repository.BeneficioRepository;
import com.lucasnogueira.ejb.Beneficio;
import com.lucasnogueira.ejb.BeneficioEjbService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BeneficioService")
class BeneficioServiceTest {

    @Mock
    private BeneficioRepository repository;

    @Mock
    private BeneficioEjbService ejbService;

    @InjectMocks
    private BeneficioService service;

    private Beneficio beneficio;

    @BeforeEach
    void setUp() {
        beneficio = new Beneficio();
        beneficio.setId(1L);
        beneficio.setNome("Plano de Saude");
        beneficio.setDescricao("Cobertura completa");
        beneficio.setValor(new BigDecimal("250.00"));
        beneficio.setAtivo(true);
    }

    // -------------------------------------------------------------------------
    // listar
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("listar deve retornar todos os beneficios")
    void listar_deveRetornarListaCompleta() {
        Beneficio outro = new Beneficio();
        outro.setId(2L);
        outro.setNome("Vale Refeicao");
        outro.setValor(new BigDecimal("500.00"));

        when(repository.findByAtivoTrue()).thenReturn(List.of(beneficio, outro));

        List<Beneficio> resultado = service.listar();

        assertEquals(2, resultado.size());
        verify(repository).findByAtivoTrue();
    }

    @Test
    @DisplayName("listar deve retornar lista vazia quando nao houver registros")
    void listar_deveRetornarListaVazia_quandoSemRegistros() {
        when(repository.findByAtivoTrue()).thenReturn(List.of());

        List<Beneficio> resultado = service.listar();

        assertTrue(resultado.isEmpty());
    }

    // -------------------------------------------------------------------------
    // buscarPorId
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("buscarPorId deve retornar o beneficio quando encontrado")
    void buscarPorId_deveRetornarBeneficio_quandoExistir() {
        when(repository.findById(1L)).thenReturn(Optional.of(beneficio));

        Beneficio resultado = service.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Plano de Saude", resultado.getNome());
    }

    @Test
    @DisplayName("buscarPorId deve lancar NoSuchElementException quando nao encontrado")
    void buscarPorId_deveLancarExcecao_quandoNaoExistir() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> service.buscarPorId(99L));

        assertTrue(ex.getMessage().contains("99"));
    }

    // -------------------------------------------------------------------------
    // criar
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("criar deve salvar beneficio com id nulo para gerar novo id")
    void criar_deveForcaIdNuloAntesDeсалvar() {
        Beneficio entrada = new Beneficio();
        entrada.setId(99L); // id fornecido pelo cliente deve ser ignorado
        entrada.setNome("Odonto");
        entrada.setValor(new BigDecimal("80.00"));

        Beneficio salvo = new Beneficio();
        salvo.setId(10L);
        salvo.setNome("Odonto");
        salvo.setValor(new BigDecimal("80.00"));

        when(repository.save(any(Beneficio.class))).thenReturn(salvo);

        Beneficio resultado = service.criar(entrada);

        assertNull(entrada.getId(), "id deve ser nulo antes do save");
        assertEquals(10L, resultado.getId());
        verify(repository).save(entrada);
    }

    @Test
    @DisplayName("criar deve retornar o beneficio persistido")
    void criar_deveRetornarBeneficioPersistido() {
        beneficio.setId(null);
        Beneficio salvo = new Beneficio();
        salvo.setId(1L);
        salvo.setNome(beneficio.getNome());
        salvo.setValor(beneficio.getValor());

        when(repository.save(beneficio)).thenReturn(salvo);

        Beneficio resultado = service.criar(beneficio);

        assertNotNull(resultado.getId());
        assertEquals(beneficio.getNome(), resultado.getNome());
    }

    // -------------------------------------------------------------------------
    // atualizar
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("atualizar deve aplicar todos os campos do payload no registro existente")
    void atualizar_deveAtualizarCamposCorretamente() {
        when(repository.findById(1L)).thenReturn(Optional.of(beneficio));

        Beneficio dados = new Beneficio();
        dados.setNome("Plano Odonto");
        dados.setDescricao("Cobertura basica");
        dados.setValor(new BigDecimal("120.00"));
        dados.setAtivo(false);

        Beneficio esperado = new Beneficio();
        esperado.setId(1L);
        esperado.setNome("Plano Odonto");
        esperado.setDescricao("Cobertura basica");
        esperado.setValor(new BigDecimal("120.00"));
        esperado.setAtivo(false);

        when(repository.save(any(Beneficio.class))).thenReturn(esperado);

        Beneficio resultado = service.atualizar(1L, dados);

        assertEquals("Plano Odonto", resultado.getNome());
        assertEquals("Cobertura basica", resultado.getDescricao());
        assertEquals(new BigDecimal("120.00"), resultado.getValor());
        assertFalse(resultado.getAtivo());
        verify(repository).save(beneficio);
    }

    @Test
    @DisplayName("atualizar deve lancar NoSuchElementException quando beneficio nao existir")
    void atualizar_deveLancarExcecao_quandoNaoExistir() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> service.atualizar(99L, new Beneficio()));
    }

    // -------------------------------------------------------------------------
    // deletar
    // -------------------------------------------------------------------------

    @Test
    @SuppressWarnings("null") // ArgumentCaptor.capture() retorna null em bytecode; conflito esperado com @NonNull do Spring Data
    @DisplayName("inativar deve marcar ativo=false e salvar o beneficio")
    void inativar_deveSalvarComAtivoFalso_quandoExistir() {
        when(repository.findById(1L)).thenReturn(Optional.of(beneficio));

        service.inativar(1L);

        ArgumentCaptor<Beneficio> captor = ArgumentCaptor.forClass(Beneficio.class);
        verify(repository).save(captor.capture());
        assertFalse(captor.getValue().getAtivo());
        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("inativar deve lancar NoSuchElementException quando beneficio nao existir")
    void inativar_deveLancarExcecao_quandoNaoExistir() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> service.inativar(99L));

        assertTrue(ex.getMessage().contains("99"));
        verify(repository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // transferir
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("transferir deve delegar ao BeneficioEjbService")
    void transferir_deveDelegarAoEjbService() {
        BigDecimal valor = new BigDecimal("200.00");

        service.transferir(1L, 2L, valor);

        verify(ejbService).transfer(1L, 2L, valor);
    }
}
