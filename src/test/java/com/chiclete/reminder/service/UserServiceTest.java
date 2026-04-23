package com.chiclete.reminder.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chiclete.reminder.domain.User;
import com.chiclete.reminder.domain.UserRole;
import com.chiclete.reminder.infra.UserRepository;
import com.chiclete.reminder.service.exception.EmailAlreadyExistsException;
import com.chiclete.reminder.ui.dto.CreateUserRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void deveRegistrarUsuarioComSucesso() {
        CreateUserRequest request = new CreateUserRequest("Joao", "joao@email.com", "senha123");
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hash123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            return u;
        });

        User created = userService.register(request);

        assertEquals("Joao", created.getName());
        assertEquals(UserRole.COMUM, created.getRole());
        assertEquals("hash123", created.getPassword());
    }

    @Test
    void deveLancarExcecaoQuandoEmailJaExiste() {
        CreateUserRequest request = new CreateUserRequest("Joao", "joao@email.com", "senha123");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> userService.register(request));
    }

    @Test
    void deveHashearSenhaAoRegistrar() {
        CreateUserRequest request = new CreateUserRequest("Joao", "joao@email.com", "senha123");
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hash123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.register(request);

        verify(passwordEncoder).encode("senha123");
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertTrue(userCaptor.getValue().getPassword().startsWith("hash"));
    }
}
