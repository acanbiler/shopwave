package com.acanbiler.shopwave.util;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * JWT utility class for token generation, validation, and parsing.
 * 
 * This utility provides:
 * - JWT token generation with user roles
 * - JWT token validation and parsing
 * - Refresh token mechanism
 * - Claims extraction
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    @Value("${jwt.issuer}")
    private String issuer;

    private static final String ROLES_CLAIM = "roles";
    private static final String USER_ID_CLAIM = "userId";
    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    /**
     * Generate JWT access token with user roles.
     * 
     * @param userDetails user details
     * @param userId user ID
     * @param roles user roles
     * @return JWT token
     */
    public String generateAccessToken(UserDetails userDetails, Long userId, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(ROLES_CLAIM, roles);
        claims.put(USER_ID_CLAIM, userId);
        claims.put(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE);
        
        return createToken(claims, userDetails.getUsername(), expiration);
    }

    /**
     * Generate JWT refresh token.
     * 
     * @param userDetails user details
     * @param userId user ID
     * @return JWT refresh token
     */
    public String generateRefreshToken(UserDetails userDetails, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(USER_ID_CLAIM, userId);
        claims.put(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE);
        
        return createToken(claims, userDetails.getUsername(), refreshExpiration);
    }

    /**
     * Create JWT token with claims.
     * 
     * @param claims token claims
     * @param subject token subject (username)
     * @param expirationTime expiration time in milliseconds
     * @return JWT token
     */
    private String createToken(Map<String, Object> claims, String subject, Long expirationTime) {        
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuer(issuer)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Extract username from JWT token.
     * 
     * @param token JWT token
     * @return username
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract user ID from JWT token.
     * 
     * @param token JWT token
     * @return user ID
     */
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get(USER_ID_CLAIM, Long.class));
    }

    /**
     * Extract roles from JWT token.
     * 
     * @param token JWT token
     * @return user roles
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return extractClaim(token, claims -> (List<String>) claims.get(ROLES_CLAIM));
    }

    /**
     * Extract token type from JWT token.
     * 
     * @param token JWT token
     * @return token type
     */
    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get(TOKEN_TYPE_CLAIM, String.class));
    }

    /**
     * Extract expiration date from JWT token.
     * 
     * @param token JWT token
     * @return expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract specific claim from JWT token.
     * 
     * @param token JWT token
     * @param claimsResolver claims resolver function
     * @param <T> claim type
     * @return claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from JWT token.
     * 
     * @param token JWT token
     * @return all claims
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            System.err.println("JWT token parsing failed: " + e.getMessage());
            throw new RuntimeException("JWT token parsing failed", e);
        }
    }

    /**
     * Check if JWT token is expired.
     * 
     * @param token JWT token
     * @return true if token is expired
     */
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Validate JWT token.
     * 
     * @param token JWT token
     * @param userDetails user details
     * @return true if token is valid
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            final String tokenType = extractTokenType(token);
            
            return (username.equals(userDetails.getUsername()) && 
                    !isTokenExpired(token) && 
                    ACCESS_TOKEN_TYPE.equals(tokenType));
        } catch (Exception e) {
            System.err.println("JWT token validation failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Validate refresh token.
     * 
     * @param token refresh token
     * @param userDetails user details
     * @return true if refresh token is valid
     */
    public Boolean validateRefreshToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            final String tokenType = extractTokenType(token);
            
            return (username.equals(userDetails.getUsername()) && 
                    !isTokenExpired(token) && 
                    REFRESH_TOKEN_TYPE.equals(tokenType));
        } catch (Exception e) {
            System.err.println("JWT refresh token validation failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if token can be refreshed.
     * 
     * @param token JWT token
     * @return true if token can be refreshed
     */
    public Boolean canTokenBeRefreshed(String token) {
        try {
            final String tokenType = extractTokenType(token);
            return ACCESS_TOKEN_TYPE.equals(tokenType) && !isTokenExpired(token);
        } catch (Exception e) {
            System.err.println("JWT token refresh check failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get signing key from secret.
     * 
     * @return signing key
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Get token expiration time in milliseconds.
     * 
     * @return expiration time
     */
    public Long getExpirationTime() {
        return expiration;
    }

    /**
     * Get refresh token expiration time in milliseconds.
     * 
     * @return refresh expiration time
     */
    public Long getRefreshExpirationTime() {
        return refreshExpiration;
    }
}