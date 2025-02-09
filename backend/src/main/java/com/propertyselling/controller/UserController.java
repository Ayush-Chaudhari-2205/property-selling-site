package com.propertyselling.controller;


import com.propertyselling.Entity.User;
import com.propertyselling.dtos.*;
import com.propertyselling.security.CustomUserDetails;
import com.propertyselling.security.JwtUtils;
import com.propertyselling.service.UserService;
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

}
