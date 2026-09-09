package com.mym.healingenv.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    public String generateToken(Long userId,String username,Integer role){
        return JWT.create()
                .withClaim("userId",userId)
                .withClaim("username",username)
                .withClaim("role",role)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expiration))
                .sign(Algorithm.HMAC256(secret));
    }

    public DecodedJWT verify(String token){
        return JWT.require(Algorithm.HMAC256(secret)).build().verify(token);
    }

    public Long getUserId(String token){
        return JWT.decode(token).getClaim("userId").asLong();
    }
}


