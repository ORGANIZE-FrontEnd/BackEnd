package com.backend.organiza.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    public enum TokenType {
        ACCESS_TOKEN,
        REFRESH_TOKEN;
    }


    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-time}")
    @Getter
    private long accessTokenExpiration;

    @Getter
    @Value("${security.refreshTokenJwt.expiration-time}")
    private long refreshTokenExpiration;



    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails, TokenType tokenType) {
        return generateToken(new HashMap<>(), userDetails, tokenType);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, TokenType tokenType) {
        return tokenType == TokenType.ACCESS_TOKEN ?
                buildToken(extraClaims, userDetails, accessTokenExpiration) :
                buildToken(extraClaims, userDetails, refreshTokenExpiration);
    }


    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        logger.info("Validating token for user: {}", username);

        if (username == null || !username.equals(userDetails.getUsername())) {
            logger.warn("Token username doesn't match user details or is null.");
            return false;
        }

        boolean isExpired = isTokenExpired(token);
        logger.info("Token expiration status for user {}: {}", username, isExpired ? "Expired" : "Valid");

        return !isExpired;
    }



    public boolean isTokenExpired(String token) {
        Date expirationDate = extractExpiration(token);
        logger.info("Token expiration date: {}", expirationDate);
        return expirationDate.before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
