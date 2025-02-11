package com.propertyselling.dao;

import com.propertyselling.Entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ReviewEntityDao extends JpaRepository<Review, Long> {

  boolean existsByBuyerIdAndPropertyId(Long buyerId, Long propertyId);

  List<Review> findByPropertyId(Long propertyId);
}
