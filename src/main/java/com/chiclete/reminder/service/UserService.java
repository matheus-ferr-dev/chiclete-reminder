package com.chiclete.reminder.service;

import com.chiclete.reminder.domain.User;
import com.chiclete.reminder.domain.UserRole;
import com.chiclete.reminder.infra.UserRepository;
import com.chiclete.reminder.service.exception.EmailAlreadyExistsException;
import com.chiclete.reminder.ui.dto.CreateUserRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Email ja cadastrado.");
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.COMUM);
        return userRepository.save(user);
    }
}
