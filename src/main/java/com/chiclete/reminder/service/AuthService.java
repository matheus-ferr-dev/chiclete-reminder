package com.chiclete.reminder.service;

import com.chiclete.reminder.domain.User;
import com.chiclete.reminder.dto.RegisterRequest;
import com.chiclete.reminder.dto.TokenResponse;
import com.chiclete.reminder.config.JwtService;
import com.chiclete.reminder.infra.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    private final GroupService groupService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            GroupService groupService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.groupService = groupService;
    }

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DomainRuleException("E-mail já registado");
        }
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole("COMUM");
        userRepository.save(user);
        groupService.acceptInviteAfterRegistration(request.inviteToken(), user);
        return new TokenResponse(jwtService.generateToken(user));
    }

    @Transactional(readOnly = true)
    public TokenResponse login(String email, String rawPassword) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, rawPassword));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DomainRuleException("Credenciais inválidas"));
        return new TokenResponse(jwtService.generateToken(user));
    }

    @Transactional
    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DomainRuleException("E-mail não encontrado"));
        user.setPassword(passwordEncoder.encode(newPassword));
    }
}
