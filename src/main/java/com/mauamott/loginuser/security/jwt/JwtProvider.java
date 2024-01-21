package com.mauamott.loginuser.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private int expiration;

    public String generateToken(UserDetails userDetails){
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("roles", userDetails.getAuthorities())
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + expiration * 1000))
                .signWith(getKey(secret))
                .compact();
    }

    public Claims getClaims(String token){
        return Jwts.parserBuilder().setSigningKey(getKey(secret)).build().parseClaimsJws(token).getBody();
    }
    public String getSubject(String token){
        return Jwts.parserBuilder().setSigningKey(getKey(secret)).build().parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validate(String token){
            try {
                Jwts.parserBuilder().setSigningKey(getKey(secret)).build().parseClaimsJws(token).getBody();
                return true;
            } catch (ExpiredJwtException e) {
                log.info("token expired");
            } catch (UnsupportedJwtException e) {
                log.info("token unsupported");
            } catch (MalformedJwtException e) {
                log.info("token malformed");
            } catch (SignatureException e) {
                log.info("token signature");
            } catch (IllegalArgumentException e) {
                log.info("token illegal");
            }
            return false;
    }
    private Key getKey(String secret){
        byte[] secretBytes = Decoders.BASE64URL.decode(secret);
        return Keys.hmacShaKeyFor(secretBytes);
    }
}
