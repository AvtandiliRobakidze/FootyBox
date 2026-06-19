package com.footybox.auth;

import com.footybox.common.ConflictException;
import com.footybox.security.JwtService;
import com.footybox.user.AppUser;
import com.footybox.user.AppUserRepository;
import com.footybox.user.UserProfileResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            AppUserRepository users,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        String username = request.username().trim();

        if (users.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Email is already registered.");
        }
        if (users.existsByUsernameIgnoreCase(username)) {
            throw new ConflictException("Username is already registered.");
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setDisplayName(request.displayName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        AppUser saved = users.save(user);

        return new AuthResponse(jwtService.createToken(saved.getEmail()), UserProfileResponse.from(saved));
    }

    public AuthResponse login(LoginRequest request) {
        String login = request.login().trim();
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(login, request.password())
        );
        AppUser user = users.findByEmailIgnoreCase(login)
                .or(() -> users.findByUsernameIgnoreCase(login))
                .orElseThrow(() -> new IllegalArgumentException("Invalid login."));
        return new AuthResponse(jwtService.createToken(user.getEmail()), UserProfileResponse.from(user));
    }
}
