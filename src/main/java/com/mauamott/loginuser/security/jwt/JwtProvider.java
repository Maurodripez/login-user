package com.mauamott.loginuser.security.jwt;

import static com.mauamott.loginuser.exception.JwtExceptions.BadTokenException;
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
                log.info("Token expired");
                throw new BadTokenException("Token expired");
            } catch (UnsupportedJwtException e) {
                log.info("Token unsupported");
                throw new BadTokenException("Token unsupported");
            } catch (MalformedJwtException e) {
                log.info("Token malformed");
                throw new BadTokenException("Token malformed");
            } catch (SignatureException e) {
                log.info("Token signature");
                throw new BadTokenException("Token signature");
            } catch (IllegalArgumentException e) {
                log.info("Token illegal");
                throw new BadTokenException("Token illegal");
            }
    }
    private Key getKey(String secret){
        byte[] secretBytes = Decoders.BASE64URL.decode(secret);
        return Keys.hmacShaKeyFor(secretBytes);
    }
}
