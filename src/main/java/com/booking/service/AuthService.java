package com.booking.service;

import com.booking.domain.Role;
import com.booking.domain.User;
import com.booking.repository.UserRepository;
import com.booking.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    /** Cadastro público: sempre cria o perfil USER. Perfis maiores só são concedidos pelo administrador. */
    @Transactional
    public void register(String name, String email, String password) {
        String normalized = email.trim().toLowerCase();
        if (users.existsByEmail(normalized)) {
            throw new ConflictException("Este e-mail já está cadastrado.");
        }
        users.save(new User(name.trim(), normalized, encoder.encode(password), Role.USER));
    }

    @Transactional(readOnly = true)
    public String login(String email, String password) {
        User user = users.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas."));
        if (!encoder.matches(password, user.getPasswordHash())) {
            throw new BadCredentialsException("Credenciais inválidas.");
        }
        return jwt.generate(user);
    }
}
