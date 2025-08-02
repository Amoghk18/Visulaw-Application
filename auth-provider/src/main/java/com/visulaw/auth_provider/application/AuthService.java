package com.visulaw.auth_provider.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visulaw.auth_provider.domain.AuthRequest;
import com.visulaw.auth_provider.domain.AuthResponse;
import com.visulaw.auth_provider.domain.LoginRequest;
import com.visulaw.auth_provider.entity.Token;
import com.visulaw.auth_provider.entity.User;
import com.visulaw.auth_provider.repository.TokenRepository;
import com.visulaw.auth_provider.repository.UserRepository;
import com.visulaw.auth_provider.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(LoginRequest request) {
        User user = new User();
        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        User persistedUser = userRepository.save(user);
        String jwtToken = jwtUtil.generateToken(user, user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user);
        saveToken(persistedUser, jwtToken);
        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        String jwtToken = jwtUtil.generateToken(user, user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user);
        revokeExistingUserToken(user);
        saveToken(user, jwtToken);
        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    private void saveToken(User persistedUser, String jwtToken) {
        Token token = Token.builder()
                .token(jwtToken)
                .user(persistedUser)
                .expired(false)
                .revoked(false)
                .build();

        tokenRepository.save(token);
    }

    private void revokeExistingUserToken(User user) {
        List<Token> tokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (CollectionUtils.isEmpty(tokens)) return;

        tokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });

        tokenRepository.saveAll(tokens);
    }

    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return;
        }

        String refreshToken = header.substring(7);
        String email = jwtUtil.getUserName(refreshToken);
        if (email != null) {
            User user = userRepository.findByEmail(email).orElseThrow();
            if (jwtUtil.isTokenValid(refreshToken, user)) {
                String newAccessToken = jwtUtil.generateToken(user, user.getRole().name());
                revokeExistingUserToken(user);
                saveToken(user, newAccessToken);
                AuthResponse authResponse = AuthResponse.builder()
                        .accessToken(newAccessToken)
                        .refreshToken(refreshToken)
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }

}
