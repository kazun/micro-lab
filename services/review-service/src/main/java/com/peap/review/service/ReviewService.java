package com.peap.review.service;

import com.peap.review.dto.CreateReviewRequest;
import com.peap.review.event.ReviewEventPublisher;
import com.peap.review.model.Review;
import com.peap.review.model.ReviewStatus;
import com.peap.review.repository.ReviewRepository;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewEventPublisher eventPublisher;

    public ReviewService(ReviewRepository reviewRepository, ReviewEventPublisher eventPublisher) {
        this.reviewRepository = reviewRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Review create(CreateReviewRequest request) {
        Review review = new Review(
                UUID.randomUUID(),
                request.entityId(),
                request.userId(),
                request.text(),
                ReviewStatus.PUBLISHED,
                Instant.now());
        reviewRepository.save(review);
        eventPublisher.publishReviewSubmitted(review);
        return review;
    }

    public List<Review> listByEntity(UUID entityId) {
        return reviewRepository.findByEntityId(entityId);
    }

    @Transactional
    public Review flag(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NoSuchElementException("review not found: " + reviewId));
        review.setStatus(ReviewStatus.FLAGGED);
        return reviewRepository.save(review);
    }
}
