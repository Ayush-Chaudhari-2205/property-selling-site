package com.propertyselling.service;

import com.propertyselling.Entity.User;
import com.propertyselling.dtos.*;

import java.util.List;

public interface UserService {
    public ApiResponse<User> addUser(UserSignupDTO dto);

    
}
