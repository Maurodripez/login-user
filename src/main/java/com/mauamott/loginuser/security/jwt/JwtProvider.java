package com.mauamott.loginuser.security.jwt;

import static com.mauamott.loginuser.exception.JwtExceptions.BadTokenException;
import static com.mauamott.loginuser.utils.ConstantsToken.*;

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
                .setExpiration(new Date(new Date().getTime() + expiration * 1000L))
                .signWith(getKey(secret))
                .compact();
    }

    public String generateTokenTemporary(UserDetails userDetails, int timeExpiration){
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("roles", userDetails.getAuthorities())
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + timeExpiration * 1000L))  // 2 minutos
                .signWith(getKey(secret))
                .compact();
    }
    public String generateTokenWithUsername(String username, int timeExpiration){
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + timeExpiration * 1000L))
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
                throw new BadTokenException(TOKEN_EXPIRED);
            } catch (UnsupportedJwtException e) {
                log.info("Token unsupported");
                throw new BadTokenException(TOKEN_UNSUPPORTED);
            } catch (MalformedJwtException e) {
                log.info("Token malformed");
                throw new BadTokenException(TOKEN_MALFORMED);
            } catch (SignatureException e) {
                log.info("Token signature");
                throw new BadTokenException(TOKEN_SIGNATURE);
            } catch (IllegalArgumentException e) {
                log.info("Token illegal");
                throw new BadTokenException(TOKEN_ILLEGAL);
            }
    }
    private Key getKey(String secret){
        byte[] secretBytes = Decoders.BASE64URL.decode(secret);
        return Keys.hmacShaKeyFor(secretBytes);
    }
}
