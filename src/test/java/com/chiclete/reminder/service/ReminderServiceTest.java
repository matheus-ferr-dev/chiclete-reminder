package com.chiclete.reminder.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chiclete.reminder.domain.Reminder;
import com.chiclete.reminder.domain.ReminderPriority;
import com.chiclete.reminder.domain.User;
import com.chiclete.reminder.infra.ReminderRepository;
import com.chiclete.reminder.infra.UserRepository;
import com.chiclete.reminder.service.exception.ReminderNotFoundException;
import com.chiclete.reminder.ui.dto.CreateReminderRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReminderServiceTest {

    @Mock
    private ReminderRepository reminderRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReminderService reminderService;

    @Test
    void deveCriarLembreteParaUsuarioAutenticado() {
        User user = user("joao@email.com", 1L);
        CreateReminderRequest request = new CreateReminderRequest(
            "Tomar remedio",
            "Apos o almoco",
            LocalDateTime.now().plusDays(1),
            ReminderPriority.ALTA,
            false,
            null
        );
        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(user));
        when(reminderRepository.save(any(Reminder.class))).thenAnswer(i -> i.getArgument(0));

        Reminder created = reminderService.create(request, "joao@email.com");

        assertEquals("Tomar remedio", created.getTitle());
        assertEquals(user, created.getUser());
        assertTrue(!created.isCompleted());
    }

    @Test
    void deveListarApenasLembretesDoUsuario() {
        User user = user("joao@email.com", 10L);
        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(user));
        when(reminderRepository.findByUserId(10L)).thenReturn(List.of(new Reminder()));

        reminderService.findAllForUser("joao@email.com");

        verify(reminderRepository).findByUserId(10L);
    }

    @Test
    void deveMarcarLembreteComoConcluido() {
        User user = user("joao@email.com", 10L);
        Reminder reminder = new Reminder();
        reminder.setCompleted(false);
        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(user));
        when(reminderRepository.findByIdAndUserId(1L, 10L)).thenReturn(Optional.of(reminder));
        when(reminderRepository.save(any(Reminder.class))).thenAnswer(i -> i.getArgument(0));

        Reminder updated = reminderService.complete(1L, "joao@email.com");

        assertTrue(updated.isCompleted());
    }

    @Test
    void deveLancarExcecaoAoCompletarLembreteDeOutroUsuario() {
        User user = user("joao@email.com", 10L);
        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(user));
        when(reminderRepository.findByIdAndUserId(99L, 10L)).thenReturn(Optional.empty());

        assertThrows(
            ReminderNotFoundException.class,
            () -> reminderService.complete(99L, "joao@email.com")
        );
    }

    private User user(String email, Long id) {
        User user = new User();
        user.setEmail(email);
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (Exception ignored) {
        }
        return user;
    }
}
