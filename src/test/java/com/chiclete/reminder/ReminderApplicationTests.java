package com.chiclete.reminder;

import com.chiclete.reminder.domain.ReminderPriority;
import com.chiclete.reminder.domain.Reminder;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários da entidade Reminder.
 */
class ReminderApplicationTests {

    @Test
    void deveCriarLembreteComCamposCorretos() {
        Reminder reminder = new Reminder();
        reminder.setTitle("Tomar remédio");
        reminder.setDescription("Tomar após o almoço");
        reminder.setScheduledAt(LocalDateTime.of(2025, 4, 1, 12, 0));
        reminder.setPriority(ReminderPriority.ALTA);
        reminder.setChewing(true);
        reminder.setCompleted(false);

        assertEquals("Tomar remédio", reminder.getTitle());
        assertEquals(ReminderPriority.ALTA, reminder.getPriority());
        assertTrue(reminder.isChewing());
        assertFalse(reminder.isCompleted());
    }

    @Test
    void deveModoChicleteComecarDesativadoPorPadrao() {
        Reminder reminder = new Reminder();

        assertFalse(reminder.isChewing());
    }

    @Test
    void deveMarcarLembreteComoConcluido() {
        Reminder reminder = new Reminder();
        reminder.setCompleted(true);

        assertTrue(reminder.isCompleted());
    }
}