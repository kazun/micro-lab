package com.peap.analytics.repository;

import com.peap.analytics.model.PlatformCounter;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author kazun
 */
public interface PlatformCounterRepository extends JpaRepository<PlatformCounter, String> {
}
