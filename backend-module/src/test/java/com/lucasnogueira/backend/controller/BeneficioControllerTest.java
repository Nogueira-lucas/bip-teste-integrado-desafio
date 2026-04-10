package com.lucasnogueira.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucasnogueira.backend.service.BeneficioService;
import com.lucasnogueira.ejb.Beneficio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BeneficioController")
class BeneficioControllerTest {

    @Mock
    private BeneficioService service;

    @InjectMocks
    private BeneficioController controller;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private Beneficio beneficio;

    @BeforeEach
    void setUp() {
        // standaloneSetup: sobe apenas o controller, sem contexto Spring nem JPA
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        beneficio = new Beneficio();
        beneficio.setId(1L);
        beneficio.setNome("Plano de Saude");
        beneficio.setDescricao("Cobertura completa");
        beneficio.setValor(new BigDecimal("250.00"));
        beneficio.setAtivo(true);
        beneficio.setVersion(0L);
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/beneficios
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /beneficios deve retornar 200 com lista de beneficios")
    void listar_deveRetornar200ComLista() throws Exception {
        when(service.listar()).thenReturn(List.of(beneficio));

        mockMvc.perform(get("/api/v1/beneficios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nome").value("Plano de Saude"));
    }

    @Test
    @DisplayName("GET /beneficios deve retornar 200 com lista vazia")
    void listar_deveRetornar200ComListaVazia() throws Exception {
        when(service.listar()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/beneficios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/beneficios/{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /beneficios/{id} deve retornar 200 com o beneficio")
    void buscar_deveRetornar200QuandoEncontrado() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(beneficio);

        mockMvc.perform(get("/api/v1/beneficios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Plano de Saude"))
                .andExpect(jsonPath("$.valor").value(250.00))
                .andExpect(jsonPath("$.ativo").value(true));
    }

    @Test
    @DisplayName("GET /beneficios/{id} deve retornar 404 quando nao encontrado")
    void buscar_deveRetornar404QuandoNaoEncontrado() throws Exception {
        when(service.buscarPorId(99L))
                .thenThrow(new NoSuchElementException("Beneficio não encontrado: 99"));

        mockMvc.perform(get("/api/v1/beneficios/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Beneficio não encontrado: 99"));
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/beneficios
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /beneficios deve retornar 201 com Location e corpo do beneficio criado")
    void criar_deveRetornar201ComLocation() throws Exception {
        Beneficio payload = new Beneficio();
        payload.setNome("Vale Refeicao");
        payload.setDescricao("Beneficio alimentar");
        payload.setValor(new BigDecimal("500.00"));
        payload.setAtivo(true);

        Beneficio salvo = new Beneficio();
        salvo.setId(2L);
        salvo.setNome("Vale Refeicao");
        salvo.setDescricao("Beneficio alimentar");
        salvo.setValor(new BigDecimal("500.00"));
        salvo.setAtivo(true);
        salvo.setVersion(0L);

        when(service.criar(any(Beneficio.class))).thenReturn(salvo);

        mockMvc.perform(post("/api/v1/beneficios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/v1/beneficios/2")))
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.nome").value("Vale Refeicao"));
    }

    // -------------------------------------------------------------------------
    // PUT /api/v1/beneficios/{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("PUT /beneficios/{id} deve retornar 200 com beneficio atualizado")
    void atualizar_deveRetornar200ComBeneficioAtualizado() throws Exception {
        Beneficio dados = new Beneficio();
        dados.setNome("Plano Odonto");
        dados.setDescricao("Cobertura basica");
        dados.setValor(new BigDecimal("120.00"));
        dados.setAtivo(false);

        Beneficio atualizado = new Beneficio();
        atualizado.setId(1L);
        atualizado.setNome("Plano Odonto");
        atualizado.setDescricao("Cobertura basica");
        atualizado.setValor(new BigDecimal("120.00"));
        atualizado.setAtivo(false);
        atualizado.setVersion(1L);

        when(service.atualizar(eq(1L), any(Beneficio.class))).thenReturn(atualizado);

        mockMvc.perform(put("/api/v1/beneficios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dados)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Plano Odonto"))
                .andExpect(jsonPath("$.ativo").value(false));
    }

    @Test
    @DisplayName("PUT /beneficios/{id} deve retornar 404 quando beneficio nao existir")
    void atualizar_deveRetornar404QuandoNaoEncontrado() throws Exception {
        when(service.atualizar(eq(99L), any(Beneficio.class)))
                .thenThrow(new NoSuchElementException("Beneficio não encontrado: 99"));

        mockMvc.perform(put("/api/v1/beneficios/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Beneficio())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Beneficio não encontrado: 99"));
    }

    // -------------------------------------------------------------------------
    // DELETE /api/v1/beneficios/{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("DELETE /beneficios/{id} deve retornar 204 sem corpo")
    void deletar_deveRetornar204() throws Exception {
        doNothing().when(service).inativar(1L);

        mockMvc.perform(delete("/api/v1/beneficios/1"))
                .andExpect(status().isNoContent());

        verify(service).inativar(1L);
    }

    @Test
    @DisplayName("DELETE /beneficios/{id} deve retornar 404 quando nao encontrado")
    void deletar_deveRetornar404QuandoNaoEncontrado() throws Exception {
        doThrow(new NoSuchElementException("Beneficio não encontrado: 99"))
                .when(service).inativar(99L);

        mockMvc.perform(delete("/api/v1/beneficios/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Beneficio não encontrado: 99"));
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/beneficios/transferencia
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /beneficios/transferencia deve retornar 200 e delegar ao service")
    void transferir_deveRetornar200() throws Exception {
        Map<String, Object> body = Map.of(
                "fromId", 1,
                "toId", 2,
                "valor", "300.00"
        );

        doNothing().when(service).transferir(1L, 2L, new BigDecimal("300.00"));

        mockMvc.perform(post("/api/v1/beneficios/transferencia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        verify(service).transferir(1L, 2L, new BigDecimal("300.00"));
    }
}
