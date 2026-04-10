package com.lucasnogueira.ejb.validator;

import com.lucasnogueira.ejb.Beneficio;
import lombok.extern.log4j.Log4j2;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public class CadeiaValidadores {

    private final List<ValidadorTransferencia> validadores;

    public CadeiaValidadores(List<ValidadorTransferencia> validadores) {
        this.validadores = new ArrayList<>();
    }

    public CadeiaValidadores() {
        this.validadores = new ArrayList<>();
    }

    public CadeiaValidadores adicionarValidador(ValidadorTransferencia validador) {
        this.validadores.add(validador);
        return this;
    }

    public void executarValidacoes(Long fromId, Long toId, BigDecimal amount, Beneficio from, Beneficio to) {
        log.debug("Executando {} validadores", validadores.size());

        for (ValidadorTransferencia validador : validadores) {
            log.trace("Executando validador: {}", validador.getClass().getSimpleName());
            validador.validar(fromId, toId, amount, from, to);
        }

        log.debug("Todas as validacoes passaram com sucesso");
    }

    public static CadeiaValidadores construirCadeiaCompleta() {
        return new CadeiaValidadores()
                .adicionarValidador(new ValidadorValorPositivo())
                .adicionarValidador(new ValidadorIdsObrigatorios())
                .adicionarValidador(new ValidadorMesmaConta())
                .adicionarValidador(new ValidadorBeneficioExistente())
                .adicionarValidador(new ValidadorSaldoSuficiente());
    }
}
