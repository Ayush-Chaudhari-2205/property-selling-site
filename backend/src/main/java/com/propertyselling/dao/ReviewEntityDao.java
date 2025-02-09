package com.propertyselling.dao;

import com.propertyselling.Entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;



public interface ReviewEntityDao extends JpaRepository<Review, Long> {


}
