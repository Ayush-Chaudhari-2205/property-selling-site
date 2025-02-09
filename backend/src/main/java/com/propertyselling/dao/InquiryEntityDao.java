package com.propertyselling.dao;

import com.propertyselling.Entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;


public interface InquiryEntityDao extends JpaRepository<Inquiry, Long> {
}
