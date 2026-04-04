package simply.Finsight_backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import simply.Finsight_backend.enums.Role;

import java.security.Key;
import java.util.Date;

public class JwtTokenProvider {

    private static final Key KEY =
            Keys.hmacShaKeyFor(JwtConstants.SECRET_KEY.getBytes());

    private static final long ACCESS_TOKEN_VALIDITY =
            1000L * 60 * 60 * 24; // 24 hours

    public static String generateToken(String email, Role role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role.name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }


    public static String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public static String getRoleFromToken(String token) {
        return (String) getClaims(token).get("role");
    }

    public static boolean isTokenValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private static Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}