package com.shih.icecms.utils;

import com.alibaba.fastjson.JSON;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Jwt工具类
 * <p>
 * 2022/1/4-22:16
 */
public class JwtUtil {

    /**
     * 有效期
     */
    public static final Long JWT_TTL = 72 * 60 * 60 * 1000L;
    /**
     * 设置秘钥明文
     */
    public static final String JWT_KEY = "lUKFWAZjS2qdiI24rAwu";


    public static String getUUID() {
        String token = UUID.randomUUID().toString().replaceAll("-", "");
        return token;
    }
    /**
     * 生成 jtw
     */
    public static String createJWT(String userName,String passWord) {
        // 设置过期时间 空
        Map<String,String> json=new HashMap<>();
        json.put("userName",userName);
        json.put("passWord",passWord);
        JwtBuilder builder = getJwtBuilder(JSON.toJSONString(json), null, getUUID());
        return builder.compact();
    }
    /**
     * 生成 jtw
     */
    public static String createJWT(Map<String, Object> payloadClaims) {
        // 设置过期时间 空
        JwtBuilder builder = getJwtBuilder("", null, getUUID());
        builder.setClaims(payloadClaims);  // and write claim to the jwt
        return builder.compact();
    }
    /**
     * 生成 jtw
     *
     * @param subject token中要存放的数据（json格式）
     * @return
     */
    public static String createJWT(String subject) {
        // 设置过期时间 空
        JwtBuilder builder = getJwtBuilder(subject, JWT_TTL, getUUID());
        return builder.compact();
    }

    /**
     * 生成 jtw
     *
     * @param subject   token中要存放的数据（json格式）
     * @param ttlMillis token超时时间
     * @return
     */
    public static String createJWT(String subject, Long ttlMillis) {
        // 设置过期时间
        JwtBuilder builder = getJwtBuilder(subject, ttlMillis, getUUID());
        return builder.compact();
    }

    private static JwtBuilder getJwtBuilder(String subject, Long ttlMillis, String uuid) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        var secretKey = generalKey();
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        if (ttlMillis == null) {
            ttlMillis = JwtUtil.JWT_TTL;
        }
        long expMillis = nowMillis + ttlMillis;
        Date expDate = new Date(expMillis);
        return Jwts.builder()
                // 唯一的ID
                .setId(uuid)
                // 主题  可以是JSON数据
                .setSubject(subject)
                // 签发者
                .setIssuer("sg")
                // 签发时间
                .setIssuedAt(now)
                // 使用 HS256 对称加密算法签名, 第二个参数为秘钥
                .signWith(signatureAlgorithm, secretKey)
                .setExpiration(expDate);
    }

    /**
     * 创建 token
     *
     * @param id
     * @param subject
     * @param ttlMillis
     * @return
     */
    public static String createJWT(String id, String subject, Long ttlMillis) {
        // 设置过期时间
        JwtBuilder builder = getJwtBuilder(subject, ttlMillis, id);
        return builder.compact();
    }

    /**
     * 生成加密后的秘钥 secretKey
     *
     * @return
     */
    public static String generalKey() {
//        byte[] encodedKey = Base64.getDecoder().decode(JwtUtil.JWT_KEY);
//        SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
        return JWT_KEY;
    }

    /**
     * 解析
     *
     * @param jwt
     * @return
     * @throws Exception
     */
    public static Claims parseJWT(String jwt) throws Exception {
        var secretKey = generalKey();
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(jwt)
                .getBody();
    }
    /**
     * 判断过期
     *
     * @param token
     * @return
     */
    public static boolean isExpire(String token) throws Exception {
        return System.currentTimeMillis() > parseJWT(token).getExpiration().getTime();
    }
    //测试方法
    public static void main(String[] args) throws Exception {
        //JWT加密
        String jwt = createJWT("123456");
        System.out.println(jwt);//eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJhYzBlYzk3ZDM0OGI0YmVkYjlmY2Q5NmZiNGViMmZkNCIsInN1YiI6IjEyMzQ1NiIsImlzcyI6InNnIiwiaWF0IjoxNjQ4OTg2NjkxLCJleHAiOjE2NDg5OTAyOTF9.G-K2XlcmE2lP7EOldbpp1rs743uvTu1NoYMo_g7sjkQ
        //JWT解密  时间过期会报错，须重新生成再解析
        Claims claims = parseJWT("eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJkZDYzZWNmYjIyYTk0MjdlYjBhZWZmNjEyZWMxODg0NCIsInN1YiI6IjEyMzQ1NiIsImlzcyI6InNnIiwiaWF0IjoxNjY1MjM5OTM3LCJleHAiOjE2NjUyODMxMzd9.nyMiypVTC4vb1qy8nTHR-l5rkwOKFghEntN6xmAD6kw");
        String subject = claims.getSubject();
        System.out.println(subject);
    }
}
