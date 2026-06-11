package com.chiclete.reminder.service;

import com.chiclete.reminder.domain.Reminder;
import com.chiclete.reminder.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    private final JavaMailSender mailSender;
    private final boolean enabled;
    private final String fromAddress;

    public EmailNotificationService(
            JavaMailSender mailSender,
            @Value("${app.mail.enabled:true}") boolean enabled,
            @Value("${app.mail.from:chiclete@localhost}") String fromAddress
    ) {
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.fromAddress = fromAddress;
    }

    public void sendReminderAlert(User user, Reminder reminder, String message) {
        if (!enabled || !user.isNotifyEmail() || user.getEmail() == null || user.getEmail().isBlank()) {
            return;
        }
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(fromAddress);
            mail.setTo(user.getEmail());
            mail.setSubject("🫧 Chiclete — " + reminder.getTitle());
            mail.setText(buildBody(user, reminder, message));
            mailSender.send(mail);
            log.info("E-mail Chiclete enviado para {}", user.getEmail());
        } catch (Exception e) {
            log.warn("Falha ao enviar e-mail Chiclete para {}: {}", user.getEmail(), e.getMessage());
        }
    }

    private String buildBody(User user, Reminder reminder, String message) {
        StringBuilder body = new StringBuilder();
        body.append("Olá ").append(user.getName()).append("!\n\n");
        body.append("Lembrete Chiclete: ").append(message).append("\n");
        body.append("Prioridade: ").append(reminder.getPriority()).append("\n");
        if (reminder.getDescription() != null && !reminder.getDescription().isBlank()) {
            body.append("\n").append(reminder.getDescription()).append("\n");
        }
        body.append("\n— Chiclete Reminder");
        return body.toString();
    }
}
