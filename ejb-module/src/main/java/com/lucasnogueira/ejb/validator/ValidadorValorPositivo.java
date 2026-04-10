package com.lucasnogueira.ejb.validator;

import com.lucasnogueira.ejb.Beneficio;
import lombok.extern.log4j.Log4j2;

import java.math.BigDecimal;

@Log4j2
public class ValidadorValorPositivo implements ValidadorTransferencia {

    @Override
    public void validar(Long fromId, Long toId, BigDecimal amount, Beneficio from, Beneficio to) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Valor invalido para transferencia: {}", amount);
            throw new IllegalArgumentException("Valor da transferência deve ser positivo");
        }
    }
}
