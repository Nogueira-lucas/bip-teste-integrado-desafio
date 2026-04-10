package com.lucasnogueira.backend.service;

import com.lucasnogueira.backend.repository.BeneficioRepository;
import com.lucasnogueira.ejb.Beneficio;
import com.lucasnogueira.ejb.BeneficioEjbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class BeneficioService {

    private final BeneficioRepository repository;
    private final BeneficioEjbService ejbService;

    @Transactional(readOnly = true)
    public List<Beneficio> listar() {
        log.debug("Listando todos os beneficios");
        List<Beneficio> resultado = repository.findByAtivoTrue();
        log.info("Total de beneficios encontrados: {}", resultado.size());
        return resultado;
    }

    @Transactional(readOnly = true)
    public Beneficio buscarPorId(Long id) {
        log.debug("Buscando beneficio com id={}", id);
        log.info("Buscando beneficio com id={}", id);
        return repository.findById(id).orElseThrow(() -> {
            log.warn("Beneficio nao encontrado para id={}", id);
            throw new NoSuchElementException("Beneficio não encontrado: " + id);
        });
    }

    public Beneficio criar(Beneficio beneficio) {
        log.debug("Criando beneficio: nome={}, valor={}", beneficio.getNome(), beneficio.getValor());
        beneficio.setId(null);
        Beneficio salvo = repository.save(beneficio);
        log.info("Beneficio criado com sucesso: id={}, nome={}", salvo.getId(), salvo.getNome());
        return salvo;
    }

    public Beneficio atualizar(Long id, Beneficio dados) {
        log.debug("Atualizando beneficio id={}: nome={}, valor={}, ativo={}",
                id, dados.getNome(), dados.getValor(), dados.getAtivo());
        Beneficio existente = buscarPorId(id);
        existente.setNome(dados.getNome());
        existente.setDescricao(dados.getDescricao());
        existente.setValor(dados.getValor());
        existente.setAtivo(dados.getAtivo());
        Beneficio atualizado = repository.save(existente);
        log.info("Beneficio atualizado com sucesso: id={}", atualizado.getId());
        return atualizado;
    }

    public Beneficio inativar(Long id) {
        log.debug("Deletando beneficio id={}", id);
        try {
            Beneficio beneficio = repository.findById(id).orElseThrow(() -> {
                log.warn("Beneficio nao encontrado para id={}", id);
                throw new NoSuchElementException("Beneficio não encontrado: " + id);
            });

            beneficio.setAtivo(false);
            repository.save(beneficio);
            log.info("Beneficio excluído com sucesso: id={}", id);
            return beneficio;
        } catch (NoSuchElementException ex) {
            log.warn("Tentativa de inativar beneficio inexistente: id={}", id);
            throw new NoSuchElementException("Falha ao inativar benefício: " + id);
        }
    }

    /**
     * Delega a transferência ao BeneficioEjbService do ejb-module.
     * O EntityManager injetado nele participa da transação Spring aberta aqui.
     */
    public ResponseEntity<String> transferir(Long fromId, Long toId, BigDecimal valor) {
        log.debug("Iniciando transferencia: fromId={}, toId={}, valor={}", fromId, toId, valor);
        try {
            ejbService.transfer(fromId, toId, valor);
        } catch (Exception ex) {
            log.error("Erro durante transferencia: fromId={}, toId={}, valor={}, error={}",
                    fromId, toId, valor, ex.getMessage(), ex);
            throw ex;
        }
        log.info("Transferencia concluida: fromId={}, toId={}, valor={}", fromId, toId, valor);
        return ResponseEntity.status(200).body("Transferência realizada com sucesso");
    }
}
