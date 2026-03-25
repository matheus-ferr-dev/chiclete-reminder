package com.chiclete.reminder.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidade que representa um lembrete no sistema.
 * Quando {@code chewing} está ativo, o lembrete reaparece até ser concluído (Modo Chiclete).
 */
@Entity
@Table(name = "reminders")
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    /** Indica se o Modo Chiclete está ativo para este lembrete. */
    @Column(nullable = false)
    private boolean chewing;

    @Column(nullable = false)
    private boolean completed;

    @Column(nullable = false)
    private String priority;

    public Reminder() {}

    public Long getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }

    public boolean isChewing() { return chewing; }
    public void setChewing(boolean chewing) { this.chewing = chewing; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}