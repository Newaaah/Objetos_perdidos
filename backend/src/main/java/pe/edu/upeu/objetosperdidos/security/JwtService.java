package pe.edu.upeu.objetosperdidos.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${security.jwt.secret-key:}")
    private String secretKey;

    @Value("${security.jwt.expiration-time:3600000}")
    private long jwtExpiration;

    @PostConstruct
    void verificarConfiguracion() {

        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException(
                    "Falta JWT_SECRET: configure una clave Base64 de 32 bytes o mas"
            );
        }

        try {
            byte[] bytes = Decoders.BASE64.decode(secretKey);

            if (bytes.length < 32) {
                throw new IllegalStateException(
                        "JWT_SECRET debe representar al menos 32 bytes aleatorios"
                );
            }

            Keys.hmacShaKeyFor(bytes);

        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException(
                    "JWT_SECRET debe estar codificado en Base64", ex
            );
        }

        if (jwtExpiration <= 0) {
            throw new IllegalStateException(
                    "security.jwt.expiration-time debe ser positivo"
            );
        }
    }

    public <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver) {

        return claimsResolver.apply(extractAllClaims(token));
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String generateToken(UserDetails userDetails) {

        Map<String, Object> extraClaims = new HashMap<>();

        if (userDetails instanceof UsuarioPrincipal principal) {
            extraClaims.put("role", principal.rol().name());
        }

        return generateToken(extraClaims, userDetails);
    }

    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails) {

        long ahora = System.currentTimeMillis();

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(ahora))
                .setExpiration(new Date(ahora + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(
            String token,
            UserDetails userDetails) {

        return extractUsername(token).equals(userDetails.getUsername())
                && !extractClaim(token, Claims::getExpiration)
                .before(new Date())
                && userDetails.isEnabled();
    }

    private Claims extractAllClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {

        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secretKey)
        );
    }
}
