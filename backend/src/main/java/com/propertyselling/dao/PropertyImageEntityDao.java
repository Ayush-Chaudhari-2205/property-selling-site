package com.propertyselling.dao;

import com.propertyselling.Entity.PropertyImage;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PropertyImageEntityDao extends JpaRepository<PropertyImage, Long> {
  List<PropertyImage> findByPropertyId(Long propertyId);


}
