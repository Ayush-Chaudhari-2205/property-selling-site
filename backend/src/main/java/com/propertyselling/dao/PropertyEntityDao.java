package com.propertyselling.dao;

import com.propertyselling.Entity.Property;


import org.springframework.data.jpa.repository.JpaRepository;


public interface PropertyEntityDao extends JpaRepository<Property,Long  >
{

}
