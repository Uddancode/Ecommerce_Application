package com.ecommerce.project.controller;

import com.ecommerce.project.model.AppRole;
import com.ecommerce.project.model.Role;
import com.ecommerce.project.model.User;
import com.ecommerce.project.repositories.RoleRepository;
import com.ecommerce.project.repositories.UserRepository;
import com.ecommerce.project.security.jwt.JwtUtils;
import com.ecommerce.project.security.request.LoginRequest;
import com.ecommerce.project.security.request.SignUpRequest;
import com.ecommerce.project.security.response.MessageResponse;
import com.ecommerce.project.security.response.UserInfoResponse;
import com.ecommerce.project.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {


    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    PasswordEncoder encoder;

    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@Valid @RequestBody LoginRequest loginRequest) {

        log.info("User attempting to log in : {}", loginRequest.getUsername());
        Authentication authentication;
        try {

            authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        } catch (AuthenticationException exception) {
            Map<String, Object> map = new HashMap<>();
            map.put("message", "Bad credentials");
            map.put("Status", false);
            System.out.println("Message" + exception.getMessage());

            log.error("SignIn endpoint user  :{} ,  this is the reason {} ", loginRequest.getUsername(), exception.getMessage());
            return new ResponseEntity<Object>(map, HttpStatus.NOT_FOUND);
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();


//        String jwtToken = jwtUtils.generateTokenFromUsername(userDetails.getUsername());

        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);


        List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        UserInfoResponse response = new UserInfoResponse(userDetails.getId(), userDetails.getUsername(), jwtCookie.toString(), roles);
//        return ResponseEntity.ok().body(response);

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString()).body(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequest signUpRequest) {

        log.info("New user registration : username {}, and email  {}", signUpRequest.getUsername(), signUpRequest.getEmail());

        if (userRepository.existsByUserName(signUpRequest.getUsername())) {

            log.warn("Username is already taken! , {}", signUpRequest.getUsername());
            return ResponseEntity.badRequest().body(new MessageResponse("Error : Username is already taken!"));
        }
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {

            log.warn("Error : Email is already in use! : {}", signUpRequest.getEmail());
            return ResponseEntity.badRequest().body(new MessageResponse("Error : Email is already in use!"));
        }

        // Creating new User's account
        User user = new User(signUpRequest.getUsername(), signUpRequest.getEmail(), encoder.encode(signUpRequest.getPassword()));

        // ROLES

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();
        if (strRoles == null) {


            Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                    .orElseThrow(() -> {

                        log.error("Error : Role is not found {}", AppRole.ROLE_USER);
                        return new RuntimeException("Error : Role is not found");
                    });
            roles.add(userRole);

        } else {


            strRoles.forEach(eachRole -> {
                        switch (eachRole) {
                            case "admin" -> {
                                Role adminRole = roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
                                        .orElseThrow(() -> {
                                            log.error("Error : Role is not found {}", AppRole.ROLE_ADMIN);
                                            return new RuntimeException("Error : Role is not found");

                                        });
                                //
                                roles.add(adminRole);
                            }
                            case "seller" -> {
                                Role sellerRole = roleRepository.findByRoleName(AppRole.ROLE_SELLER)
                                        .orElseThrow(() -> {

                                            log.error("Error : Role is not found {}", AppRole.ROLE_SELLER);
                                            return new RuntimeException("Error : Role is not found");
                                        });
                                roles.add(sellerRole);
                            }
                            default -> {
                                Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                                        .orElseThrow(() -> {
                                            log.error("Error : Role is not found {}", AppRole.ROLE_USER);
                                            return new RuntimeException("Error : Role is not found");
                                        });
                                roles.add(userRole);
                            }
                        }

                    }

            );


        }
        user.setRoles(roles);
        User savedUser = userRepository.save(user);

        log.info("User saved successfully : {},{}, {}", savedUser.getUserId(), savedUser.getUserName(), savedUser.getEmail());
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','SELLER','USER')")
    @GetMapping("/username")
    public ResponseEntity<?> getCurrentUserName(Authentication authentication) {

        if (authentication != null) {

            log.info("Fetching current username , {}", authentication.getName());
            return ResponseEntity.ok().body(authentication.getName());
        } else {

            log.warn("No user logged in!");
            return ResponseEntity.badRequest().body(new MessageResponse("Error : no user logged in!"));
        }

    }

    @PreAuthorize("hasAnyRole('ADMIN','SELLER','USER')")

    @GetMapping("/user")
    public ResponseEntity<?> getUserInfo(Authentication authentication) {

        if (authentication != null) {

            log.info("Fetching current username , {}", authentication.getName());

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();


            List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
            UserInfoResponse response = new UserInfoResponse(userDetails.getId(), userDetails.getUsername(), roles);

            return ResponseEntity.ok().body(response);
        } else {

            log.warn("No user found!");
            return ResponseEntity.badRequest().body(new MessageResponse("Error : no user found!"));
        }


    }


    @PreAuthorize("hasAnyRole('ADMIN','SELLER','USER')")
    @GetMapping("/sellers")
    public String getAllSellers() {
        return "sellers ";
    }


    @PreAuthorize("hasAnyRole('ADMIN','SELLER','USER')")
    @PostMapping("/signout")
    public ResponseEntity<?> signOutUser() {
        try {


            ResponseCookie cookie = jwtUtils.getCleanJWTCookie();

            log.info("Log out end point");
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(
                    new MessageResponse("Logged out successfully!")
            );
        } catch (Exception e) {

            log.warn("Log out end point , {}", e.getMessage());
            return ResponseEntity.ok().body(new MessageResponse("Error : failed to logout!"));
        }
    }


}
