package com.lucasnogueira.ejb.validator;

import com.lucasnogueira.ejb.Beneficio;
import lombok.extern.log4j.Log4j2;

import java.math.BigDecimal;

@Log4j2
public class ValidadorMesmaConta implements ValidadorTransferencia {

    @Override
    public void validar(Long fromId, Long toId, BigDecimal amount, Beneficio from, Beneficio to) {
        if (fromId != null && fromId.equals(toId)) {
            log.error("Tentativa de transferencia para a mesma conta: id={}", fromId);
            throw new IllegalArgumentException("Não é possível transferir para a mesma conta");
        }
    }
}
