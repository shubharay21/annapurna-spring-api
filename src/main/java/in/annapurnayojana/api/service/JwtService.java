package in.annapurnayojana.api.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class JwtService {

    private final Map<String, String> cache = new ConcurrentHashMap<>();
    private final String secret;
    private final String issuer;
    private final String audience;
    private final int expiryMinutes;
    private final int refreshExpiryDays;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.issuer}") String issuer,
                      @Value("${app.jwt.audience}") String audience,
                      @Value("${app.jwt.expirationMinutes:60}") int expiryMinutes,
                      @Value("${app.jwt.refreshExpirationDays:7}") int refreshExpiryDays) {
        this.secret = secret;
        this.issuer = issuer;
        this.audience = audience;
        this.expiryMinutes = expiryMinutes;
        this.refreshExpiryDays = refreshExpiryDays;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public TokenPair generateTokens(String mobileNumber, UUID applicationId) {
        long now = System.currentTimeMillis();
        String accessJti = UUID.randomUUID().toString();
        
        // Access Token
        String accessToken = Jwts.builder()
                .issuer(issuer)
                .audience().add(audience).and()
                .claim("mobilephone", mobileNumber)
                .claim("applicationId", applicationId != null ? applicationId.toString() : "")
                .claim("token_type", "access")
                .id(accessJti)
                .issuedAt(new Date(now))
                .expiration(new Date(now + TimeUnit.MINUTES.toMillis(expiryMinutes)))
                .signWith(getSigningKey())
                .compact();

        // Refresh Token
        String refreshJti = UUID.randomUUID().toString();
        String refreshToken = Jwts.builder()
                .issuer(issuer)
                .audience().add(audience).and()
                .claim("mobilephone", mobileNumber)
                .claim("applicationId", applicationId != null ? applicationId.toString() : "")
                .claim("token_type", "refresh")
                .id(refreshJti)
                .issuedAt(new Date(now))
                .expiration(new Date(now + TimeUnit.DAYS.toMillis(refreshExpiryDays)))
                .signWith(getSigningKey())
                .compact();

        cache.put("jwt:" + accessJti, "valid");

        return new TokenPair(accessToken, refreshToken);
    }

    public void revokeToken(String jti) {
        if (jti != null) {
            cache.remove("jwt:" + jti);
        }
    }

    public boolean isTokenValid(String jti) {
        if (jti == null) return false;
        String value = cache.get("jwt:" + jti);
        return "valid".equals(value);
    }

    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .requireIssuer(issuer)
                    .requireAudience(audience)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    public record TokenPair(String accessToken, String refreshToken) {}
}
