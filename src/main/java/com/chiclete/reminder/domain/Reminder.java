package com.chiclete.reminder.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidade que representa um lembrete no sistema.
 * Quando {@code chewing} está ativo, o lembrete reaparecer em intervalos
 * definidos até que o usuário confirme a conclusão (Modo Chiclete).
 * A cada vez ignorado, {@code ignoreCount} é incrementado e pode elevar a prioridade.
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

    @Column(nullable = false)
    private boolean chewing;

    /** Intervalo em minutos entre repetições no Modo Chiclete. */
    @Column
    private Integer intervalMinutes;

    /** Contador de vezes que o lembrete foi ignorado pelo usuário. */
    @Column(nullable = false)
    private int ignoreCount = 0;

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

    public Integer getIntervalMinutes() { return intervalMinutes; }
    public void setIntervalMinutes(Integer intervalMinutes) { this.intervalMinutes = intervalMinutes; }

    public int getIgnoreCount() { return ignoreCount; }
    public void setIgnoreCount(int ignoreCount) { this.ignoreCount = ignoreCount; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}