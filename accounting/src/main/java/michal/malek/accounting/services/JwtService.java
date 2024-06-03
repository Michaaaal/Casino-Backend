package michal.malek.accounting.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class JwtService {
    public JwtService(@Value("${jwt.secret}") String secret){
        SECRET = secret;
    }
    public final String SECRET;


    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
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

}
