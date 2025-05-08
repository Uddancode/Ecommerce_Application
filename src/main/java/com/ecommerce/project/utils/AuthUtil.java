package com.ecommerce.project.utils;

import com.ecommerce.project.model.User;
import com.ecommerce.project.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuthUtil {
    @Autowired
    UserRepository userRepository;

    // LoggedInUser
    public User loggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findUserByUserName(authentication.getName())
                .orElseThrow(() -> {
                    log.warn("User not found with username :" + authentication.getName());
                    return new UsernameNotFoundException("User not found with username :" + authentication.getName());
                });
    }


    // LoggedInUserId

    public Long loggedInUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findUserByUserName(authentication.getName())
                .orElseThrow(() -> {
                    log.warn("User not found with username :" + authentication.getName());
                    return new UsernameNotFoundException("User not found with username :" + authentication.getName());
                });


        return user.getUserId();
    }

    // LoggedInEmail
    public String loggedInEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findUserByUserName(authentication.getName())
                .orElseThrow(() -> {
                    log.warn("User not found with username :" + authentication.getName());
                    return new UsernameNotFoundException("User not found with username :" + authentication.getName());
                });
        return user.getEmail();
    }
}
