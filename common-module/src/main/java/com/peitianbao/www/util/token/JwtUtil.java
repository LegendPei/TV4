package com.peitianbao.www.util.token;

import com.peitianbao.www.util.LoadProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Properties;
/**
 * @author leg
 */
public class JwtUtil {
    //配置token有效时间
    static String configFileName = "application.properties";
    static Properties properties = LoadProperties.load(configFileName);
    public static final long EXPIRATION_TIME =Long.parseLong(properties.getProperty("tk.EXPIRATION_TIME"));
    public static final long REFRESH_EXPIRATION_TIME =Long.parseLong(properties.getProperty("tk.REFRESH_EXPIRATION_TIME"));

    //获取密钥
    private static SecretKey getSigningKey() {
        return JwtKeyGenerator.getSecretKey();
    }

    /**
     * 生成 Access Token
     */
    public static String generateAccessToken(int userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId)) //设置主体内容
                .issuedAt(new Date())            //设置签发时间
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) //设置过期时间
                .signWith(getSigningKey())       //使用密钥签名
                .compact();
    }

    /**
     * 生成 Refresh Token
     */
    public static String generateRefreshToken(int userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId)) //设置主体内容
                .issuedAt(new Date())            //设置签发时间
                .expiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION_TIME)) //设置过期时间
                .signWith(getSigningKey())       //使用密钥签名
                .compact();
    }

    /**
     * 验证并解析 Token
     */
    public static Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey()) //验证签名
                    .build()
                    .parseSignedClaims(token)    //解析 Token
                    .getPayload();               //获取负载
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从Token中解析出userId
     */
    public static Integer parseUserIdFromToken(String token) {
        try {
            //验证并解析Token
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey()) //验证签名
                    .build()
                    .parseSignedClaims(token)    //解析Token
                    .getPayload();               //获取负载

            //从Subject中提取userId
            String subject = claims.getSubject();
            if (subject == null || subject.isEmpty()) {
                throw new IllegalArgumentException("Token subject is empty");
            }

            return Integer.parseInt(subject);
        } catch (Exception e) {
            return null;
        }
    }
}