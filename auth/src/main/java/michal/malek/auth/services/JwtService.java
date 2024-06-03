package michal.malek.auth.services;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtService {

    public JwtService(@Value("${jwt.secret}") String secret){
        SECRET = secret;
    }
    public final String SECRET;

    public void validateToken(final String token) throws ExpiredJwtException, IllegalArgumentException {
        Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token);
    }
    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(long userId,String username,int exp){
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        return createToken(claims,username,exp);
    }
    public String createToken(Map<String,Object> claims, String username,int exp){
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+exp))
                .signWith(getSignKey(), SignatureAlgorithm.HS256).compact();
    }

    public String getSubject(final String token){
        return Jwts
                .parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public long getClaimUserId(final String token){
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token);

            Claims claims = claimsJws.getBody();

            return Long.parseLong(claims.get("userId").toString());
        } catch (Exception e) {
            throw new RuntimeException("Unable to extract user ID from token", e);
        }
    }
    public String refreshToken(final String token, int exp){
        String username = getSubject(token);
        long userId = getClaimUserId(token);
        return generateToken(userId,username,exp);
    }
}

