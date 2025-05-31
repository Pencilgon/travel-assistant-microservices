package com.carrental.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtGenerator {
    private static final String JWT_SECRET = "fh378fh3fh82367g2ff9859065j8g94h758gjhgkhhui56773gds5d6";
    public String generateToken(Authentication authentication){
        String username = authentication.getName();
        Date issuedDate = new Date(System.currentTimeMillis());
        Date expirationDate = new Date(issuedDate.getTime()+1000*60*60*24);
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(issuedDate)
                .setExpiration(expirationDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    private Key getSigningKey(){
        byte[] key = Decoders.BASE64.decode(JWT_SECRET);
        return Keys.hmacShaKeyFor(key);
    }
    public Claims getAllClaims(String token){
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
    }
    public String getUsername(String token){
        return getAllClaims(token).getSubject();
    }
    public Boolean validateToken(String token){
        try{
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        }catch (Exception e){
            throw new AuthenticationCredentialsNotFoundException("Token is not correct or expired!!!");
        }
    }
}
