package com.lucasnogueira.ejb;

import com.lucasnogueira.ejb.validator.CadeiaValidadores;
import com.lucasnogueira.ejb.validator.ValidadorSaldoSuficiente;
import com.lucasnogueira.ejb.validator.ValidadorTransferencia;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;

import java.math.BigDecimal;
import java.util.ArrayList;

@Log4j2
@Stateless
public class BeneficioEjbService {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        log.debug("Iniciando transferencia: fromId={}, toId={}, amount={}", fromId, toId, amount);

        // Buscar entidades com lock pessimista para evitar condições de corrida
        Beneficio from = em.find(Beneficio.class, fromId, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        Beneficio to = em.find(Beneficio.class, toId, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);

        // Validação de entrada
        CadeiaValidadores validadores = CadeiaValidadores.construirCadeiaCompleta();
        validadores.executarValidacoes(fromId, toId, amount, from, to);

        BigDecimal saldoOrigem = from.getValor();
        BigDecimal saldoDestino = to.getValor();

        log.debug("Saldo atual — origem: {}, destino: {}", saldoOrigem, saldoDestino);

        // Realizar transferência
        BigDecimal novoSaldoOrigem = saldoOrigem.subtract(amount);
        BigDecimal novoSaldoDestino = saldoDestino.add(amount);

        from.setValor(novoSaldoOrigem);
        to.setValor(novoSaldoDestino);

        // Flush para garantir que as alterações sejam persistidas
        em.flush();

        log.info("Transferencia concluida com sucesso: fromId={} (saldo: {} -> {}), toId={} (saldo: {} -> {}), amount={}",
                fromId, saldoOrigem, novoSaldoOrigem, toId, saldoDestino, novoSaldoDestino, amount);
    }
}
