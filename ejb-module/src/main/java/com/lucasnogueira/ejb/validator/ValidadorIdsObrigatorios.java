package com.lucasnogueira.ejb.validator;

import com.lucasnogueira.ejb.Beneficio;
import lombok.extern.log4j.Log4j2;

import java.math.BigDecimal;

@Log4j2
public class ValidadorIdsObrigatorios implements ValidadorTransferencia {

    @Override
    public void validar(Long fromId, Long toId, BigDecimal amount, Beneficio from, Beneficio to) {
        if (fromId == null || toId == null) {
            log.error("IDs invalidos: fromId={}, toId={}", fromId, toId);
            throw new IllegalArgumentException("IDs de origem e destino são obrigatórios");
        }
    }
}
