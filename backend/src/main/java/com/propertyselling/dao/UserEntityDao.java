package com.propertyselling.dao;

import com.propertyselling.Entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserEntityDao extends JpaRepository<User,Long> {

    Optional<User> findByEmail(String email);


    Optional<User> findByMobileNumber(String mobileNumber);

}
