package com.booking.security;

import com.booking.domain.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/** Emite tokens JWT assinados (HS256). O token carrega o id do usuário e o perfil. */
@Service
public class JwtService {

    private final JwtEncoder encoder;
    private final Clock clock;
    private final Duration ttl;

    public JwtService(JwtEncoder encoder, Clock clock, @Value("${app.jwt.ttl-minutes}") long ttlMinutes) {
        this.encoder = encoder;
        this.clock = clock;
        this.ttl = Duration.ofMinutes(ttlMinutes);
    }

    public String generate(User user) {
        Instant now = Instant.now(clock);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("secure-booking-platform")
                .issuedAt(now)
                .expiresAt(now.plus(ttl))
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("roles", List.of(user.getRole().name()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long ttlSeconds() { return ttl.toSeconds(); }
}
