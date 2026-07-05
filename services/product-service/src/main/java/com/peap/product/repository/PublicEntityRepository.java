package com.peap.product.repository;

import com.peap.product.model.PublicEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicEntityRepository extends JpaRepository<PublicEntity, UUID> {

    List<PublicEntity> findByCategory(String category);
}
