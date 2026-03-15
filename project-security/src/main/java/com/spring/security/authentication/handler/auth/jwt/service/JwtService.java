package com.spring.security.authentication.handler.auth.jwt.service;

import com.spring.security.authentication.handler.auth.jwt.constant.JWTConstants;
import com.spring.security.authentication.handler.auth.jwt.dto.JwtTokenUserLoginInfo;
import com.spring.security.common.web.enums.BaseCode;
import com.spring.security.common.web.exception.BaseException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtService {

    @Value("${spring.app.jwtSecret}")
    private String jwtSecret;

    private final JsonMapper jsonMapper;

    public String getJwtFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith(JWTConstants.BEARER_PREFIX)) {
            return bearerToken.substring(JWTConstants.BEARER_PREFIX.length());
        }
        return null;
    }

    public String getUserNameFromJwtToken(String authToken) {
        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(authToken)
                .getPayload()
                .getSubject();
    }

    public String generateTokenFromUsername(
            String username, JwtTokenUserLoginInfo jwtTokenUserLoginInfo, long expiredTime) {
        String json = jsonMapper.writeValueAsString(jwtTokenUserLoginInfo);
        return Jwts.builder()
                .subject(username)
                .claim(JWTConstants.USER_INFO, json)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + expiredTime))
                .signWith(key())
                .compact();
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public JwtTokenUserLoginInfo validateJwtToken(String authToken) {
        try {
            Jws<Claims> claimsJws =
                    Jwts.parser().verifyWith((SecretKey) key()).build().parseSignedClaims(authToken);
            Claims claims = claimsJws.getPayload();
            String json = claims.get(JWTConstants.USER_INFO, String.class);
            return jsonMapper.readValue(json, JwtTokenUserLoginInfo.class);
        } catch (MalformedJwtException e) {
            log.error("JWT Token 无效: {}", e.getMessage());
            throw new BaseException(BaseCode.TOKEN_MALFORMED);
        } catch (ExpiredJwtException e) {
            log.error("JWT 已过期: {}", e.getMessage());
            throw new BaseException(BaseCode.TOKEN_EXPIRED);
        } catch (UnsupportedJwtException e) {
            log.error("JWT 不受支持: {}", e.getMessage());
            throw new BaseException(BaseCode.TOKEN_UNSUPPORTED);
        } catch (IllegalArgumentException e) {
            log.error("JWT 内容为空: {}", e.getMessage());
            throw new BaseException(BaseCode.TOKEN_EMPTY);
        } catch (Exception e) {
            log.error("JWT 解析异常: {}", e.getMessage());
            throw new BaseException(BaseCode.TOKEN_PARSE_ERROR);
        }
    }
}
