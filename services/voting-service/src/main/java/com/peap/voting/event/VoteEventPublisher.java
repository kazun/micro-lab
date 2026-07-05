package com.peap.voting.event;

import com.peap.voting.model.Vote;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class VoteEventPublisher {

    private static final String TOPIC = "vote-submitted";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public VoteEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishVoteSubmitted(Vote vote) {
        VoteSubmittedEvent event = new VoteSubmittedEvent(
                vote.getEntityId(), vote.getUserId(), vote.getValue(), vote.getUpdatedAt());
        kafkaTemplate.send(TOPIC, vote.getEntityId().toString(), event);
    }
}
