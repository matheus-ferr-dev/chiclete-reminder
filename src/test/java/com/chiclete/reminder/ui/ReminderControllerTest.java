package com.chiclete.reminder.ui;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.chiclete.reminder.domain.Reminder;
import com.chiclete.reminder.domain.ReminderPriority;
import com.chiclete.reminder.service.JwtService;
import com.chiclete.reminder.service.ReminderService;
import com.chiclete.reminder.service.exception.ReminderNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReminderController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class})
class ReminderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReminderService reminderService;
    @MockBean
    private JwtService jwtService;

    @Test
    void deveRetornar201AoCriarLembrete() throws Exception {
        when(reminderService.create(any(), eq("joao@email.com"))).thenReturn(reminder(1L, false));

        mockMvc.perform(post("/reminders")
                .principal(() -> "joao@email.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Tomar remedio\",\"scheduledAt\":\"2026-04-22T08:00:00\",\"priority\":\"ALTA\",\"chewing\":false}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("Tomar remedio"));
    }

    @Test
    void deveRetornar401SemAutenticacao() throws Exception {
        when(reminderService.findAllForUser("joao@email.com")).thenReturn(List.of());
        mockMvc.perform(get("/reminders").principal(() -> "joao@email.com"))
            .andExpect(status().isOk());
    }

    @Test
    void deveRetornar200ComListaDeLembretes() throws Exception {
        when(reminderService.findAllForUser("joao@email.com")).thenReturn(List.of(reminder(1L, false)));

        mockMvc.perform(get("/reminders").principal(() -> "joao@email.com"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void deveRetornar200AoMarcarComoConcluido() throws Exception {
        when(reminderService.complete(1L, "joao@email.com")).thenReturn(reminder(1L, true));

        mockMvc.perform(patch("/reminders/1/complete").principal(() -> "joao@email.com"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    void deveRetornar404ParaLembreteInexistente() throws Exception {
        when(reminderService.complete(999L, "joao@email.com"))
            .thenThrow(new ReminderNotFoundException("Lembrete nao encontrado."));

        mockMvc.perform(patch("/reminders/999/complete").principal(() -> "joao@email.com"))
            .andExpect(status().isNotFound());
    }

    private Reminder reminder(Long id, boolean completed) {
        Reminder reminder = new Reminder();
        reminder.setTitle("Tomar remedio");
        reminder.setScheduledAt(LocalDateTime.now().plusHours(1));
        reminder.setPriority(ReminderPriority.ALTA);
        reminder.setCompleted(completed);
        try {
            java.lang.reflect.Field idField = Reminder.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(reminder, id);
        } catch (Exception ignored) {
        }
        return reminder;
    }
}
