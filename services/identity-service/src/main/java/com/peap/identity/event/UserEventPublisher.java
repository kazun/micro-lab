package com.peap.identity.event;

import com.peap.identity.model.User;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @author kazun
 */
@Component
public class UserEventPublisher {

    private static final String TOPIC = "user-created";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public UserEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishUserCreated(User user) {
        UserCreatedEvent event = new UserCreatedEvent(
                user.getId(), user.getEmail(), user.getRole().name(), user.getCreatedAt());
        kafkaTemplate.send(TOPIC, user.getId().toString(), event);
    }
}
