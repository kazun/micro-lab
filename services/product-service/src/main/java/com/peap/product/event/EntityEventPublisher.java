package com.peap.product.event;

import com.peap.product.model.PublicEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EntityEventPublisher {

    private static final String TOPIC = "entity-created";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public EntityEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishEntityCreated(PublicEntity entity) {
        EntityCreatedEvent event = new EntityCreatedEvent(
                entity.getId(), entity.getName(), entity.getCategory(), entity.getCreatedAt());
        kafkaTemplate.send(TOPIC, entity.getId().toString(), event);
    }
}
