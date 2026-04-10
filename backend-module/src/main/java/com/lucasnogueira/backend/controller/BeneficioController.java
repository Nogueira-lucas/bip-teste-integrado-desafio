package com.lucasnogueira.backend.controller;

import com.lucasnogueira.backend.service.BeneficioService;
import com.lucasnogueira.ejb.Beneficio;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/beneficios")
public class BeneficioController {

    private final BeneficioService service;

    public BeneficioController(BeneficioService service) {
        this.service = service;
    }

    @GetMapping
    public List<Beneficio> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Beneficio> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
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
    public ResponseEntity<Beneficio> atualizar(@PathVariable Long id,
                                               @RequestBody Beneficio beneficio) {
        return ResponseEntity.ok(service.atualizar(id, beneficio));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/transferencia")
    public ResponseEntity<Void> transferir(@RequestBody Map<String, Object> body) {
        Long fromId = Long.valueOf(body.get("fromId").toString());
        Long toId   = Long.valueOf(body.get("toId").toString());
        BigDecimal valor = new BigDecimal(body.get("valor").toString());
        service.transferir(fromId, toId, valor);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(404).body(Map.of("erro", ex.getMessage()));
    }
}
