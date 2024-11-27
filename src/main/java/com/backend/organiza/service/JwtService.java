package com.backend.organiza.service;

import io.jsonwebtoken.*;
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
        REFRESH_TOKEN
    }

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-time}")
    @Getter
    private long accessTokenExpiration;

    @Value("${security.refreshTokenJwt.expiration-time}")
    @Getter
    private long refreshTokenExpiration;

    /**
     * Extrai o nome de usuário (subject) do token JWT.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrai um claim específico do token.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Gera um token com base no tipo (ACCESS/REFRESH).
     */
    public String generateToken(UserDetails userDetails, TokenType tokenType, String userId) {
        return generateToken(new HashMap<>(), userDetails, tokenType, userId);
    }

    /**
     * Gera um token com claims adicionais.
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, TokenType tokenType, String userId) {
        long expiration = (tokenType == TokenType.ACCESS_TOKEN) ? accessTokenExpiration : refreshTokenExpiration;
        return buildToken(extraClaims, userDetails, expiration, userId);
    }

    /**
     * Constrói o token JWT.
     */
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration,
            String userId
    ) {
        logger.info("Building JWT for user: {}", userDetails.getUsername());
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .setId(userId)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Verifica se um token é válido para um usuário.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);

            if (username == null || !username.equals(userDetails.getUsername())) {
                logger.warn("Token username doesn't match or is null.");
                return false;
            }

            if (isTokenExpired(token)) {
                logger.warn("Token for user {} is expired.", username);
                return false;
            }

            logger.info("Token is valid for user: {}", username);
            return true;
        } catch (JwtException e) {
            logger.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verifica se o token está expirado.
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expirationDate = extractExpiration(token);
            return expirationDate.before(new Date());
        } catch (JwtException e) {
            logger.warn("Failed to check token expiration: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Extrai a data de expiração do token.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrai todos os claims do token.
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            logger.error("Failed to parse JWT: {}", e.getMessage());
            throw e; // Você pode lançar uma exceção customizada aqui
        }
    }

    /**
     * Retorna a chave de assinatura.
     */
    private Key getSignInKey() {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("Secret key is not configured properly!");
        }
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
