package com.lucasnogueira.ejb;

import jakarta.ejb.Singleton;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "beneficio")
@Getter
@Setter
public class Beneficio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "descricao", length = 255)
    private String descricao;

    @Column(name = "valor", nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(name = "ativo")
    private Boolean ativo = Boolean.TRUE;

    @Version
    @Column(name = "version")
    private Long version = 0L;

}
