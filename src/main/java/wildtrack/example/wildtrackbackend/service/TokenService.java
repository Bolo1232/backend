package wildtrack.example.wildtrackbackend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class TokenService {
    private final Key secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256); // Generate a secure key

    // Token expiration time (e.g., 1 day)
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000;

    // Generate a JWT token
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(secretKey)
                .compact();
    }

    // Validate and parse the token
    public String verifyToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject(); // Returns the email or identifier set as the subject
    }
}
