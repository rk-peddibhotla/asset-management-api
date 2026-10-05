
package com.ravikanth.asset_management.auth;

import com.ravikanth.asset_management.user.User;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;

    public JwtService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    // Generate a JWT token for an authenticated user
    public String generateToken(User user) {

        // Get the current time
        Instant now = Instant.now();

        // Build the information stored inside the JWT
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("asset-management-api")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .build();

        // Sign the JWT using the HS256 algorithm
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256)
                .build();

        // Encode the claims and return the generated token
        return jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }
}

