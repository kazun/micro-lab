package com.peap.product.service;

import com.peap.product.dto.CreateEntityRequest;
import com.peap.product.event.EntityEventPublisher;
import com.peap.product.model.EntityStatus;
import com.peap.product.model.PublicEntity;
import com.peap.product.repository.PublicEntityRepository;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EntityService {

    private final PublicEntityRepository repository;
    private final EntityEventPublisher eventPublisher;

    public EntityService(PublicEntityRepository repository, EntityEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public PublicEntity create(CreateEntityRequest request) {
        PublicEntity entity = new PublicEntity(
                UUID.randomUUID(),
                request.name(),
                request.category(),
                request.description(),
                EntityStatus.PENDING,
                Instant.now());
        repository.save(entity);
        eventPublisher.publishEntityCreated(entity);
        return entity;
    }

    public PublicEntity getById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("entity not found: " + id));
    }

    public List<PublicEntity> listByCategory(String category) {
        return category == null ? repository.findAll() : repository.findByCategory(category);
    }
}
