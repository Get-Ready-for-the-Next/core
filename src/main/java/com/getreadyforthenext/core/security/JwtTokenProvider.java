package com.getreadyforthenext.core.security;

import com.getreadyforthenext.core.config.JwtConfig;
import com.getreadyforthenext.core.model.User;
import com.getreadyforthenext.core.repository.UserRepository;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtTokenProvider {

    private final JwtConfig jwtConfig;

    private final UserRepository userRepository;

    public JwtTokenProvider(JwtConfig jwtConfig, UserRepository userRepository) {
        this.jwtConfig = jwtConfig;
        this.userRepository = userRepository;
    }

    public String generateAccessToken(User user) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + this.jwtConfig.getAccessExpirationTime());

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("name", user.getName());
        userInfo.put("email", user.getEmail());
        userInfo.put("role", user.getRole().toString());

        Map<String, Object> claims = new HashMap<>();
        claims.put("user", userInfo);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuer("getreadyforthenext.com")
                .setAudience("getreadyforthenext.com")
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, this.jwtConfig.getAccessSecret())
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtConfig.getAccessSecret()).parseClaimsJws(token);
            return true;
        } catch (SignatureException exception) {
            log.error("Invalid JWT signature - {}", exception.getMessage());
        } catch (MalformedJwtException exception) {
            log.error("Invalid JWT token - {}", exception.getMessage());
        } catch (ExpiredJwtException exception) {
            log.error("Expired JWT token - {}", exception.getMessage());
        } catch (UnsupportedJwtException exception) {
            log.error("Unsupported JWT token - {}", exception.getMessage());
        } catch (IllegalArgumentException exception) {
            log.error("JWT claims string is empty - {}", exception.getMessage());
        }
        return false;
    }


    public Claims getClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(jwtConfig.getAccessSecret()).parseClaimsJws(token).getBody();
    }

    public User getUserFromAccessToken(String token) {
        Claims claims = getClaimsFromToken(token);
        Map userClaims = claims.get("user", Map.class);
        Long userId = (((Number) userClaims.get("id")).longValue());
        return this.userRepository.findById(userId).orElseThrow();
    }
}