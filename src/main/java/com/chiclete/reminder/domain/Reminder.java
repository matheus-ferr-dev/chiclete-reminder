package com.chiclete.reminder.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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

    /** Momento do último alerta enviado (in-app ou externo). */
    @Column
    private LocalDateTime lastNotifiedAt;

    /** Adiamento temporário — não dispara alertas até este momento. */
    @Column
    private LocalDateTime snoozedUntil;

    /** NONE, WEEKDAYS ou CUSTOM — lembrete recorrente nos dias configurados. */
    @Column(nullable = false)
    private String recurrenceType = RecurrenceSupport.NONE;

    /** Dias ISO-8601 (1=seg … 7=dom), ex.: "1,2,3,4,5". */
    @Column
    private String recurrenceDays;

    /** Quando falso, a recorrência fica pausada sem eliminar o lembrete. */
    @Column(nullable = false)
    private boolean recurringEnabled = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /** Destinatários com acesso ao lembrete (leitura e ações permitidas pelo serviço). */
    @ManyToMany
    @JoinTable(
        name = "reminder_shares",
        joinColumns = @JoinColumn(name = "reminder_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> sharedWith = new HashSet<>();

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

    public LocalDateTime getLastNotifiedAt() { return lastNotifiedAt; }
    public void setLastNotifiedAt(LocalDateTime lastNotifiedAt) { this.lastNotifiedAt = lastNotifiedAt; }

    public LocalDateTime getSnoozedUntil() { return snoozedUntil; }
    public void setSnoozedUntil(LocalDateTime snoozedUntil) { this.snoozedUntil = snoozedUntil; }

    public String getRecurrenceType() { return recurrenceType; }
    public void setRecurrenceType(String recurrenceType) { this.recurrenceType = recurrenceType; }

    public String getRecurrenceDays() { return recurrenceDays; }
    public void setRecurrenceDays(String recurrenceDays) { this.recurrenceDays = recurrenceDays; }

    public boolean isRecurringEnabled() { return recurringEnabled; }
    public void setRecurringEnabled(boolean recurringEnabled) { this.recurringEnabled = recurringEnabled; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public Set<User> getSharedWith() { return sharedWith; }
    public void setSharedWith(Set<User> sharedWith) { this.sharedWith = sharedWith; }
}