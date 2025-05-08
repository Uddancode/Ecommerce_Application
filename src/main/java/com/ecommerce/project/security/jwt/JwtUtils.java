package com.ecommerce.project.security.jwt;

import com.ecommerce.project.security.services.UserDetailsImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtUtils {


    // Secret Key -  custom to user

    @Value("${spring.app.secret}")
    private String jwtSecret;

    @Value("${spring.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    @Value("${spring.app.jwtCookieName}")
    private String jwtCookieName;


    // Get JwtCookieName

    public String getJwtFromCookies(HttpServletRequest request) {

        Cookie cookie = WebUtils.getCookie(request, jwtCookieName);
        if (cookie != null) {

            return cookie.getValue();
        } else {
            return null;
        }
    }

    //Create cookie

    public ResponseCookie generateJwtCookie(UserDetailsImpl userDetails) {
        String jwt = generateTokenFromUsername(userDetails.getUsername());
        ResponseCookie cookie = ResponseCookie
                .from(jwtCookieName, jwt)
                .path("/api")
                .maxAge(24 * 60 * 60)
                .httpOnly(false)
                .build();
        return cookie;
    }

    // Clean cookie

    public ResponseCookie getCleanJWTCookie() {
        ResponseCookie cookie = ResponseCookie.from("ecom_cookie", null)
                .path("/api").build();
        return cookie;
    }
    // Extract JWt token from Header

    public String getJwtFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        // bearerToken => Bearer dfghjkhgfdrtylkjhgfdfghj
        log.debug("Authorization Header : {}", bearerToken);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            // extract the Token


            return bearerToken.substring(7);
        }

        return null;
    }

    // Generate Token from Username

    public String generateTokenFromUsername(String username) {


        //  Jwt
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + jwtExpirationMs))
                .signWith(key())
                .compact();

    }

    public String getUsernameFromJwtToken(String token) {

        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }


    public boolean validateJwtToken(String authToken) {


        try {
            System.out.println("Validate method ");

            Jwts.parser()
                    .verifyWith((SecretKey) key())
                    .build()
                    .parseSignedClaims(authToken);
            System.out.println(" valid JWT ");
            return true;
        } catch (MalformedJwtException e) {
            log.error("invalid JWT Token :{}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("Jwt token is expired :{}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("unsupported JWT Token :{}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Jwt claims String is empty :{}", e.getMessage());
        }


        return false;
    }


}
