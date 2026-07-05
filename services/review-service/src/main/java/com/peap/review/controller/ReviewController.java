package com.peap.review.controller;

import com.peap.review.dto.CreateReviewRequest;
import com.peap.review.dto.ReviewResponse;
import com.peap.review.service.ReviewService;
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
import org.springframework.web.bind.annotation.RestController;

/**
 * @author kazun
 */
@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> create(@Valid @RequestBody CreateReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ReviewResponse.from(reviewService.create(request)));
    }

    @GetMapping("/entity/{entityId}")
    public ResponseEntity<List<ReviewResponse>> listByEntity(@PathVariable UUID entityId) {
        List<ReviewResponse> reviews = reviewService.listByEntity(entityId).stream()
                .map(ReviewResponse::from)
                .toList();
        return ResponseEntity.ok(reviews);
    }

    @PostMapping("/{id}/flag")
    public ResponseEntity<ReviewResponse> flag(@PathVariable UUID id) {
        return ResponseEntity.ok(ReviewResponse.from(reviewService.flag(id)));
    }
}
