package com.propertyselling.controller;


import com.propertyselling.Entity.User;
import com.propertyselling.dtos.*;
import com.propertyselling.security.CustomUserDetails;
import com.propertyselling.security.JwtUtils;
import com.propertyselling.service.UserService;

import io.swagger.v3.oas.models.responses.ApiResponse;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils utils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private ModelMapper modelMapper;

    @PostMapping("/signup")
    public ResponseEntity<?> signUpUser(@RequestBody UserSignupDTO dto) {
        System.out.println("working in User Controller:"+dto.toString());
        return ResponseEntity.ok(userService.addUser(dto));
    }

        @PostMapping("/signin")
        public ResponseEntity<?> signinUser(@RequestBody @Valid SigninRequestDTO reqDTO) {
            System.out.println("in signin " + reqDTO);
            // simply invoke authentucate(...) on AuthMgr
            // i/p : Authentication => un verifed credentials
            // i/f --> Authentication --> imple by UsernamePasswordAuthToken
            // throws exc OR rets : verified credentials (UserDetails i.pl class: custom
            // user details)

            Authentication verifiedAuth = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken
                            (reqDTO.getEmail(), reqDTO.getPassword()));
    //        System.out.println("getClass"+verifiedAuth.getClass());// Custom user details
    //        System.out.println("getDetails"+verifiedAuth.getDetails());// Custom user details
    //        System.out.println("getName"+verifiedAuth.getName());// Custom user details
    //        User authenticatedUser = (User) verifiedAuth.getPrincipal();
    //        // Convert User entity to UserResponseDTO
    //        UserResponseDTO userResponseDTO = modelMapper.map(authenticatedUser, UserResponseDTO.class);

            CustomUserDetails userDetails = (CustomUserDetails)verifiedAuth.getPrincipal();
            User user = userDetails.getUser();

            // Check if the user account is active
            if (user.isActive()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>("User account is inactive. Please contact support.", null));
            }

             SigninResponseDTO localUser = new SigninResponseDTO();

             localUser.setJwt(utils.generateJwtToken(verifiedAuth));
             localUser.setEmail(user.getEmail());
             localUser.setUser_id(user.getId());
             localUser.setFullName(user.getFullName());
             localUser.setRole(user.getUserType());
             localUser.setMobileNo(user.getMobileNumber());



            // => auth success
            return ResponseEntity
                    .ok(localUser);

        }
    // get user based on id
    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getUserProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }

    //get user based on email
    @GetMapping("/profile/get-by-email")
    public ResponseEntity<?> getUserByEmail(@RequestParam String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @PutMapping("/aadhaar")
    public ResponseEntity<?> addAadhaarCard(@RequestBody AadhaarRequestDTO dto) {
        return ResponseEntity.ok(userService.addAadhaarCard(dto));
    }

    @PutMapping("/security-question")
    public ResponseEntity<?> addSecurityQuestion(@RequestBody SecurityQuestionDTO dto) {
        return ResponseEntity.ok(userService.addSecurityQuestion(dto));
    }

    @PutMapping("/address/{userId}")
    public ResponseEntity<?> addOrUpdateAddress(@PathVariable Long userId, @RequestBody AddressDTO addressDTO) {
        return ResponseEntity.ok(userService.addOrUpdateAddress(userId, addressDTO));
    }

    @PutMapping("/update/{userId}")
    public ResponseEntity<?> updateUserProfile(@PathVariable Long userId, @RequestBody UserUpdateDTO dto) {
        return ResponseEntity.ok(userService.updateUserProfile(userId, dto));
    }

    @PutMapping("/update/update-by-email")
    public ResponseEntity<?> updateUserByEmail(@RequestParam String email, @RequestBody UserUpdateDTO dto) {
        return ResponseEntity.ok(userService.updateUserByEmail(email, dto));
    }

    @GetMapping("/count")
    public ResponseEntity<?> countUsersExcludingAdmins() {
        return ResponseEntity.ok(userService.countUsersExcludingAdmins());
    }

    @GetMapping("/non-admin-users")
    public ResponseEntity<?> getAllNonAdminUsers() {
        return ResponseEntity.ok(userService.getAllNonAdminUsers());
    }

    @PutMapping("/status")
    public ResponseEntity<?> updateUserStatus(@RequestBody UserStatusUpdateDTO dto) {
        return ResponseEntity.ok(userService.updateUserStatus(dto));
    }

}
