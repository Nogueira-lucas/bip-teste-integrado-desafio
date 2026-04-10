package com.lucasnogueira.backend.service;

import com.lucasnogueira.backend.repository.BeneficioRepository;
import com.lucasnogueira.ejb.Beneficio;
import com.lucasnogueira.ejb.BeneficioEjbService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class BeneficioService {

    private final BeneficioRepository repository;
    private final BeneficioEjbService ejbService;

    public BeneficioService(BeneficioRepository repository, BeneficioEjbService ejbService) {
        this.repository = repository;
        this.ejbService = ejbService;
    }

    @Transactional(readOnly = true)
    public List<Beneficio> listar() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Beneficio buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Beneficio não encontrado: " + id));
    }

    public Beneficio criar(Beneficio beneficio) {
        beneficio.setId(null);
        return repository.save(beneficio);
    }

    public Beneficio atualizar(Long id, Beneficio dados) {
        Beneficio existente = buscarPorId(id);
        existente.setNome(dados.getNome());
        existente.setDescricao(dados.getDescricao());
        existente.setValor(dados.getValor());
        existente.setAtivo(dados.getAtivo());
        return repository.save(existente);
    }

    public void deletar(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("Beneficio não encontrado: " + id);
        }
        repository.deleteById(id);
    }

    /**
     * Delega a transferência ao BeneficioEjbService do ejb-module.
     * O EntityManager injetado nele participa da transação Spring aberta aqui.
     */
    public void transferir(Long fromId, Long toId, BigDecimal valor) {
        ejbService.transfer(fromId, toId, valor);
    }
}
