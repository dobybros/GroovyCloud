package com.docker.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.impl.DefaultJwtBuilder;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lick on 2019/6/27.
 * Description：
 */
public class JWTUtils {
    public static final String secretkey = "GROOVYCLOUDQILIAO";

    public static String createToken(String key, Map<String, Object> claims, Long expireTime) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("alg", "HS256");
        map.put("typ", "JWT");
        JwtBuilder jwtBuilder = new DefaultJwtBuilder();
        if (claims != null) {
            jwtBuilder.setClaims(claims);
        }
        if (expireTime != null) {
            jwtBuilder.setExpiration(new Date(System.currentTimeMillis() + expireTime));
        }
        String token = jwtBuilder
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS256, secretkey + key)
                .compact();
        return token;
    }

    public static Claims getClaims(String key, String token) {
        return Jwts.parser().setSigningKey(secretkey + key).parseClaimsJws(token).getBody();
    }

    public static Claims getClaimsIgnoreExpire(String key, String token) {
        try {
            return Jwts.parser().setSigningKey(JWTUtils.secretkey + key).parseClaimsJws(token).getBody();
        }catch (ExpiredJwtException e){
            return e.getClaims();
        }
    }
}

