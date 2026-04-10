package com.lucasnogueira.ejb.validator;

import com.lucasnogueira.ejb.Beneficio;

import java.math.BigDecimal;

public interface ValidadorTransferencia {
    void validar(Long fromId, Long toId, BigDecimal amount, Beneficio from, Beneficio to);
}
