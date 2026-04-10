package com.lucasnogueira.ejb.validator;

import com.lucasnogueira.ejb.Beneficio;
import lombok.extern.log4j.Log4j2;

import java.math.BigDecimal;

@Log4j2
public class ValidadorSaldoSuficiente implements ValidadorTransferencia {

    @Override
    public void validar(Long fromId, Long toId, BigDecimal amount, Beneficio from, Beneficio to) {
        if (from != null && from.getValor().compareTo(amount) < 0) {
            log.warn("Saldo insuficiente em origem (id={}): saldo={}, amount={}",
                    fromId, from.getValor(), amount);
            throw new IllegalStateException("Saldo insuficiente para transferência");
        }
    }
}
