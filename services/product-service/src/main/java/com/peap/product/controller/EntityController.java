package com.peap.product.controller;

import com.peap.product.dto.CreateEntityRequest;
import com.peap.product.dto.EntityResponse;
import com.peap.product.service.EntityService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author kazun
 */
@RestController
@RequestMapping("/api/v1/entities")
public class EntityController {

    private final EntityService entityService;

    public EntityController(EntityService entityService) {
        this.entityService = entityService;
    }

    @PostMapping
    public ResponseEntity<EntityResponse> create(@Valid @RequestBody CreateEntityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(EntityResponse.from(entityService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(EntityResponse.from(entityService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<List<EntityResponse>> list(@RequestParam(required = false) String category) {
        List<EntityResponse> entities = entityService.listByCategory(category).stream()
                .map(EntityResponse::from)
                .toList();
        return ResponseEntity.ok(entities);
    }
}
