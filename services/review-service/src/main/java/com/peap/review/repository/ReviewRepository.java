package com.peap.review.repository;

import com.peap.review.model.Review;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author kazun
 */
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findByEntityId(UUID entityId);
}
