package com.lucasnogueira.ejb.validator;

import com.lucasnogueira.ejb.Beneficio;
import lombok.extern.log4j.Log4j2;

import java.math.BigDecimal;

@Log4j2
public class ValidadorBeneficioExistente implements ValidadorTransferencia {

    @Override
    public void validar(Long fromId, Long toId, BigDecimal amount, Beneficio from, Beneficio to) {
        if (from == null) {
            log.error("Beneficio de origem nao encontrado: id={}", fromId);
            throw new IllegalArgumentException("Benefício de origem não encontrado");
        }

        if (to == null) {
            log.error("Beneficio de destino nao encontrado: id={}", toId);
            throw new IllegalArgumentException("Benefício de destino não encontrado");
        }
    }
}
