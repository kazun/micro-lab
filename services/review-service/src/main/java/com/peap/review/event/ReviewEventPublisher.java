package com.peap.review.event;

import com.peap.review.model.Review;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ReviewEventPublisher {

    private static final String TOPIC = "review-submitted";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ReviewEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishReviewSubmitted(Review review) {
        ReviewSubmittedEvent event = new ReviewSubmittedEvent(
                review.getId(), review.getEntityId(), review.getUserId(), review.getCreatedAt());
        kafkaTemplate.send(TOPIC, review.getEntityId().toString(), event);
    }
}
