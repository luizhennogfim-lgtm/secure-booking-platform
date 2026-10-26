package com.booking.web;

import com.booking.security.JwtService;
import com.booking.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService auth;
    private final JwtService jwt;

    public AuthController(AuthService auth, JwtService jwt) {
        this.auth = auth;
        this.jwt = jwt;
    }

    public record RegisterRequest(@NotBlank String name, @NotBlank @Email String email,
                                  @NotBlank @Size(min = 8, message = "deve ter pelo menos 8 caracteres") String password) {}

    public record LoginRequest(@NotBlank String email, @NotBlank String password) {}

    public record TokenResponse(String accessToken, String tokenType, long expiresInSeconds) {}

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody RegisterRequest r) {
        auth.register(r.name(), r.email(), r.password());
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest r) {
        return new TokenResponse(auth.login(r.email(), r.password()), "Bearer", jwt.ttlSeconds());
    }
}
