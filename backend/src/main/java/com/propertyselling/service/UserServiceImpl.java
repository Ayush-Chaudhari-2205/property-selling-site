package com.propertyselling.service;

import com.propertyselling.Entity.*;
import com.propertyselling.dao.UserEntityDao;
import com.propertyselling.dtos.*;

import io.swagger.v3.oas.models.responses.ApiResponse;

import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserEntityDao userEntityDao;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PasswordEncoder encoder;

    @Override
    public ApiResponse<User> addUser(UserSignupDTO dto) {
        if (userEntityDao.findByEmail(dto.getEmail()).isPresent()) {
            return new ApiResponse<>("Email already registered!", null);
        }

        if (userEntityDao.findByMobileNumber(dto.getMobileNumber()).isPresent()) {
            return new ApiResponse<>("Mobile number already registered!", null);
        }
        User newUser = modelMapper.map(dto, User.class);
//        user.setPassword(encoder.encode(user.getPassword()));//pwd : encrypted using SHA
        newUser.setPassword(encoder.encode(dto.getPassword()));
        User savedUser = userEntityDao.save(newUser);
        return new ApiResponse<>("User registered successfully!", savedUser);
    }

    @Override
    public ApiResponse<UserProfileDTO> getUserProfile(Long userId) {
        Optional<User> userOpt = userEntityDao.findById(userId);

        if (userOpt.isEmpty()) {
            return new ApiResponse<>("User not found!", null);
        }

        User user = userOpt.get();

        //  Convert entity to DTO using ModelMapper
        UserProfileDTO userProfile = modelMapper.map(user, UserProfileDTO.class);

        //  Manually map address to avoid lazy loading issues
        if (user.getAddress() != null) {
            userProfile.setAddress(modelMapper.map(user.getAddress(), AddressDTO.class));
        }

        //  Handle Aadhaar separately to avoid null pointer exceptions
        userProfile.setAadhaarNumber(user.getAadhaarCard() != null ? user.getAadhaarCard().getCardNo() : null);

        return new ApiResponse<>("User profile retrieved successfully!", userProfile);
    }

    @Override
    public ApiResponse<?> addAadhaarCard(AadhaarRequestDTO dto) {
        Optional<User> userOpt = userEntityDao.findById(dto.getUserId());

        if (userOpt.isEmpty()) {
            return new ApiResponse<>("User not found!", null);
        }

        User user = userOpt.get();

        //  Check if user already has an Aadhaar card
        AadhaarCard aadhaarCard = user.getAadhaarCard();
        if (aadhaarCard == null) {
            aadhaarCard = new AadhaarCard();
            user.setAadhaarCard(aadhaarCard);
        }

        //  Update Aadhaar details
        aadhaarCard.setCardNo(dto.getAadhaarNumber());

        userEntityDao.save(user); // Save updated user details

        return new ApiResponse<>("Aadhaar card details added/updated successfully!", null);
    }

    @Override
    @Transactional
    public ApiResponse<?> addSecurityQuestion(SecurityQuestionDTO dto) {
        Optional<User> userOpt = userEntityDao.findById(dto.getUserId());

        if (userOpt.isEmpty()) {
            return new ApiResponse<>("User not found!", null);
        }

        User user = userOpt.get();

        //  Update Security Question & Answer
        user.setQuestion(dto.getQuestion());
        user.setAnswer(dto.getAnswer());

        userEntityDao.save(user); // Save updated user details

        return new ApiResponse<>("Security question and answer added/updated successfully!", null);
    }

    @Override
    @Transactional
    public ApiResponse<?> addOrUpdateAddress(Long userId, AddressDTO addressDTO) {
        Optional<User> userOpt = userEntityDao.findById(userId);

        if (userOpt.isEmpty()) {
            return new ApiResponse<>("User not found!", null);
        }

        User user = userOpt.get();

        //  Check if the user already has an address
        Address address = user.getAddress();
        if (address == null) {
            address = new Address();
            user.setAddress(address);
        }

        //  Map DTO to entity
        modelMapper.map(addressDTO, address);

        userEntityDao.save(user); // Save updated user details

        return new ApiResponse<>("Address added/updated successfully!", null);
    }

    @Override
    @Transactional
    public ApiResponse<?> updateUserProfile(Long userId, UserUpdateDTO dto) {
        Optional<User> userOpt = userEntityDao.findById(userId);

        if (userOpt.isEmpty()) {
            return new ApiResponse<>("User not found!", null);
        }

        User user = userOpt.get();

        //  Use ModelMapper to update non-null fields only
        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
        modelMapper.map(dto, user);

        //  Update Aadhaar details if provided
        if (dto.getAadhaarNumber() != null) {
            if (user.getAadhaarCard() == null) {
                user.setAadhaarCard(new AadhaarCard());
            }
            user.getAadhaarCard().setCardNo(dto.getAadhaarNumber());
        }

        //  Update address using ModelMapper
        if (dto.getAddress() != null) {
            if (user.getAddress() == null) {
                user.setAddress(new Address());
            }
            modelMapper.map(dto.getAddress(), user.getAddress());
        }

        userEntityDao.save(user);  // Save updated user details

        return new ApiResponse<>("User profile updated successfully!", null);
    }

    @Override
    public ApiResponse<Long> countUsersExcludingAdmins() {
        Long userCount = userEntityDao.countByUserTypeNot(UserType.ADMIN);
        return new ApiResponse<>("Total users excluding admins retrieved successfully!", userCount);
    }

    @Override
    public ApiResponse<UserProfileDTO> getUserByEmail(String email) {
        Optional<User> userOpt = userEntityDao.findByEmail(email);

        if (userOpt.isEmpty()) {
            return new ApiResponse<>("User not found!", null);
        }

        User user = userOpt.get();

        //  Convert entity to DTO using ModelMapper
        UserProfileDTO userProfile = modelMapper.map(user, UserProfileDTO.class);

        //  Ensure Aadhaar number is included
        userProfile.setAadhaarNumber(user.getAadhaarCard() != null ? user.getAadhaarCard().getCardNo() : null);

        //  Ensure address is included
        if (user.getAddress() != null) {
            userProfile.setAddress(modelMapper.map(user.getAddress(), AddressDTO.class));
        }

        return new ApiResponse<>("User retrieved successfully!", userProfile);
    }

    @Override
    @Transactional
    public ApiResponse<?> updateUserByEmail(String email, UserUpdateDTO dto) {
        Optional<User> userOpt = userEntityDao.findByEmail(email);

        if (userOpt.isEmpty()) {
            return new ApiResponse<>("User not found!", null);
        }

        User user = userOpt.get();

        //  Use ModelMapper to update non-null fields only
        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
        modelMapper.map(dto, user);

        //  Update Aadhaar details if provided
        if (dto.getAadhaarNumber() != null) {
            if (user.getAadhaarCard() == null) {
                user.setAadhaarCard(new AadhaarCard());
            }
            user.getAadhaarCard().setCardNo(dto.getAadhaarNumber());
        }

        //  Update address using ModelMapper
        if (dto.getAddress() != null) {
            if (user.getAddress() == null) {
                user.setAddress(new Address());
            }
            modelMapper.map(dto.getAddress(), user.getAddress());
        }

        userEntityDao.save(user);  // Save updated user details

        return new ApiResponse<>("User profile updated successfully!", null);
    }

    @Override
    public ApiResponse<List<UserProfileDTO>> getAllNonAdminUsers() {
        List<User> nonAdminUsers = userEntityDao.findByUserTypeNot(UserType.ADMIN);

        if (nonAdminUsers.isEmpty()) {
            return new ApiResponse<>("No active non-admin users found!", null);
        }

        List<UserProfileDTO> userDTOList = nonAdminUsers.stream()
                .map(user -> modelMapper.map(user, UserProfileDTO.class))
                .collect(Collectors.toList());

        return new ApiResponse<>("Active non-admin users retrieved successfully!", userDTOList);
    }

    @Override
    public ApiResponse<?> updateUserStatus(UserStatusUpdateDTO dto) {
        // Verify that the admin exists and is an admin
        Optional<User> adminOpt = userEntityDao.findById(dto.getAdminId());
        if (adminOpt.isEmpty() || adminOpt.get().getUserType() != UserType.ADMIN) {
            return new ApiResponse<>("Unauthorized! Only admins can update user status.", null);
        }

        // Fetch the target user to update
        Optional<User> userOpt = userEntityDao.findById(dto.getUserId());
        if (userOpt.isEmpty()) {
            return new ApiResponse<>("User not found!", null);
        }

        User user = userOpt.get();
        user.setActive(dto.getIsActive());
        userEntityDao.save(user);

        String status = dto.getIsActive() ? "activated" : "deactivated";
        return new ApiResponse<>("User has been " + status + " successfully!", null);
    }

    


}
