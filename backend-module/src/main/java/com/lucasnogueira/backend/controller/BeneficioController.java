package com.lucasnogueira.backend.controller;

import com.lucasnogueira.backend.service.BeneficioService;
import com.lucasnogueira.ejb.Beneficio;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Tag(name = "Benefícios", description = "Gerenciamento de benefícios")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/beneficios")
public class BeneficioController {

    private final BeneficioService service;

    @GetMapping
    public List<Beneficio> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Beneficio> buscar(@PathVariable("id") Long id) {
        try {
            Beneficio beneficio = service.buscarPorId(id);
            return ResponseEntity.ok(beneficio);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(404).body(null);
        }
    }

    @PostMapping
    public ResponseEntity<Beneficio> criar(@RequestBody Beneficio beneficio) {
        Beneficio salvo = service.criar(beneficio);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(salvo.getId())
                .toUri();
        return ResponseEntity.created(location).body(salvo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Beneficio> atualizar(@PathVariable("id") Long id,
                                               @RequestBody Beneficio beneficio) {
        return ResponseEntity.ok(service.atualizar(id, beneficio));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deletar(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.inativar(id));
    }

    @PostMapping("/transferencia")
    public ResponseEntity<String> transferir(@RequestBody Map<String, Object> body) {
        Long fromId = Long.valueOf(body.get("fromId").toString());
        Long toId   = Long.valueOf(body.get("toId").toString());
        BigDecimal valor = new BigDecimal(body.get("valor").toString());
        return service.transferir(fromId, toId, valor);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(404).body(Map.of("erro", ex.getMessage()));
    }
}
