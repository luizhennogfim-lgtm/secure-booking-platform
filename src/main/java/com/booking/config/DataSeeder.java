package com.booking.config;

import com.booking.domain.Role;
import com.booking.domain.User;
import com.booking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Cria um administrador e um prestador de exemplo (só se ainda não existirem). */
@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final String adminPassword;
    private final String providerPassword;

    public DataSeeder(UserRepository users, PasswordEncoder encoder,
                      @Value("${app.seed.admin-password}") String adminPassword,
                      @Value("${app.seed.provider-password}") String providerPassword) {
        this.users = users;
        this.encoder = encoder;
        this.adminPassword = adminPassword;
        this.providerPassword = providerPassword;
    }

    @Override
    public void run(String... args) {
        createIfMissing("Administrador", "admin@booking.com", adminPassword, Role.ADMIN);
        createIfMissing("Prestador Demo", "provider@booking.com", providerPassword, Role.PROVIDER);
    }

    private void createIfMissing(String name, String email, String password, Role role) {
        if (!users.existsByEmail(email)) {
            users.save(new User(name, email, encoder.encode(password), role));
        }
    }
}
