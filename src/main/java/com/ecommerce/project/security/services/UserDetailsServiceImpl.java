package com.ecommerce.project.security.services;

import com.ecommerce.project.model.User;
import com.ecommerce.project.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UserRepository userRepository;
    @Transactional
    @Override
    public UserDetailsImpl loadUserByUsername(String username) throws UsernameNotFoundException {
        // User Repository
        User user = userRepository.findUserByUserName(username).orElseThrow(() -> new UsernameNotFoundException("User not found with username : " + username));
        return UserDetailsImpl.build(user);
    }




}
