package com.ecommerce.project.security;

import com.ecommerce.project.model.AppRole;
import com.ecommerce.project.model.Role;
import com.ecommerce.project.model.User;
import com.ecommerce.project.repositories.RoleRepository;
import com.ecommerce.project.repositories.UserRepository;
import com.ecommerce.project.security.jwt.AuthEntryPointJwt;
import com.ecommerce.project.security.jwt.AuthTokenFilter;
import com.ecommerce.project.security.services.UserDetailsServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Set;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {
    // No need of Datasource - User repo -> UserDetailsServiceImpl

    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Autowired
    private AuthEntryPointJwt authEntryPointJwt;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {


        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsServiceImpl);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // use custom algo- if you any
        return new BCryptPasswordEncoder();
    }

    // Main -> Security filter chain

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // stateless


        http.csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(authEntryPointJwt))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(request ->
                                request.requestMatchers("/api/auth/signin").permitAll()
                                        .requestMatchers("/api/auth/signup").permitAll()
                                        // Restrictions
                                        // RBAC
                                        .requestMatchers("/api/public/**").permitAll()
                                        // Allow only Admin & Seller
                                        .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "SELLER")
                                        // Users
                                        .requestMatchers("/api/addresses/**").hasAnyRole("ADMIN", "SELLER", "USER")

                                        .requestMatchers("/api/carts/**").hasAnyRole("ADMIN", "SELLER", "USER")
                                        .requestMatchers("/api/order/**").hasAnyRole("ADMIN", "SELLER", "USER")


// get all the carts
                                        .requestMatchers("/v3/api-docs/**").permitAll()
                                        .requestMatchers("/swagger-ui/**").permitAll()
                                        .requestMatchers("/api/test/**").permitAll()
                                        .requestMatchers("/h2-console/**").permitAll()
                                        .anyRequest().authenticated()


                );

        http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));

        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    //

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web -> web.ignoring().requestMatchers(
                "/configuration/ui",
                "/swagger-resources/**",
                "/configuration/security",
                "/swagger-ui.html",
                "/webjars/**"
        ));

    }

    @Bean
    public CommandLineRunner initData(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {

        return args -> {
            // Retrieve / Create roles
            Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER).orElseGet(
                    () -> {
                        Role newUserRole = new Role(AppRole.ROLE_USER);
                        return roleRepository.save(newUserRole);
                    }
            );

            Role sellerRole = roleRepository.findByRoleName(AppRole.ROLE_SELLER).orElseGet(
                    () -> {
                        Role newSellerRole = new Role(AppRole.ROLE_SELLER);
                        return roleRepository.save(newSellerRole);
                    }
            );
            Role adminRole = roleRepository.findByRoleName(AppRole.ROLE_ADMIN).orElseGet(
                    () -> {
                        Role newAdminRole = new Role(AppRole.ROLE_ADMIN);
                        return roleRepository.save(newAdminRole);
                    }
            );


            Set<Role> userRoles = Set.of(userRole);
            Set<Role> sellerRoles = Set.of(sellerRole);
            Set<Role> adminRoles = Set.of(userRole, sellerRole, adminRole);

            // Create user if not already present

            if (!userRepository.existsByUserName("johndoe")) {

                User user1 = new User("johndoe", "johndoe@gmail.com", passwordEncoder().encode("john@1234"));

                userRepository.save(user1);
            }

            if (!userRepository.existsByUserName("seller1")) {

                User seller1 = new User("seller1", "seller1@gmail.com", passwordEncoder().encode("seller123"));

                userRepository.save(seller1);
            }
            if (!userRepository.existsByUserName("admin")) {

                User admin = new User("admin", "admin@gmail.com", passwordEncoder().encode("admin123"));

                userRepository.save(admin);
            }

            // Update Roles for existing users

            userRepository.findUserByUserName("johndoe").ifPresent(user -> {

                user.setRoles(userRoles);
                userRepository.save(user);
            });
            userRepository.findUserByUserName("seller1").ifPresent(seller -> {

                seller.setRoles(sellerRoles);
                userRepository.save(seller);
            });
            userRepository.findUserByUserName("admin").ifPresent(admin -> {

                admin.setRoles(adminRoles);
                userRepository.save(admin);
            });

        };

    }


}
