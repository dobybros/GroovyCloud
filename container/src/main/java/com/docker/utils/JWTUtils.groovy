package com.docker.utils

import io.jsonwebtoken.JwtBuilder
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.impl.DefaultJwtBuilder

/**
 * Created by lick on 2019/6/27.
 * Description：
 */
class JWTUtils {
    public static final String secretkey = "GROOVYCLOUDQILIAO"
    public static String createToken(String key, Map claims, Long expireTime) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("alg", "HS256");
        map.put("typ", "JWT");
        JwtBuilder jwtBuilder = new DefaultJwtBuilder();
        if(claims != null){
            jwtBuilder.setClaims(claims)
        }
        if(expireTime != null){
            jwtBuilder.setExpiration(new Date(System.currentTimeMillis() + expireTime))
        }
        String token = jwtBuilder
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS256, secretkey + key)
                .compact()
        return token
    }
}
