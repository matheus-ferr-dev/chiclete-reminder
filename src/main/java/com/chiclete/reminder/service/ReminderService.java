package com.chiclete.reminder.service;

import com.chiclete.reminder.domain.RecurrenceSupport;
import com.chiclete.reminder.domain.Reminder;
import com.chiclete.reminder.domain.User;
import com.chiclete.reminder.dto.ReminderRequest;
import com.chiclete.reminder.dto.ReminderResponse;
import com.chiclete.reminder.infra.ReminderRepository;
import com.chiclete.reminder.infra.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class ReminderService {

    private static final List<String> PRIORITY_ORDER = List.of("BAIXA", "MEDIA", "ALTA", "URGENTE");

    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;
    private final int ignoreThreshold;

    public ReminderService(
            ReminderRepository reminderRepository,
            UserRepository userRepository,
            @Value("${app.chewing.ignore-threshold:3}") int ignoreThreshold
    ) {
        this.reminderRepository = reminderRepository;
        this.userRepository = userRepository;
        this.ignoreThreshold = ignoreThreshold;
    }

    @Transactional(readOnly = true)
    public List<ReminderResponse> listVisible(User user) {
        return reminderRepository.findAllVisibleToUser(user.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReminderResponse get(Long id, User user) {
        Reminder r = reminderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Lembrete não encontrado"));
        if (!canView(r, user)) {
            throw new NotFoundException("Lembrete não encontrado");
        }
        return toResponse(r);
    }

    @Transactional
    public ReminderResponse create(ReminderRequest request, User owner) {
        Reminder r = new Reminder();
        applyRequest(r, request);
        r.setOwner(owner);
        r.setCompleted(false);
        r.setIgnoreCount(0);
        reminderRepository.save(r);
        return toResponse(r);
    }

    @Transactional
    public ReminderResponse update(Long id, ReminderRequest request, User user) {
        Reminder r = getOwnedReminderOrThrow(id, user);
        applyRequest(r, request);
        reminderRepository.save(r);
        return toResponse(r);
    }

    @Transactional
    public void delete(Long id, User user) {
        Reminder r = getOwnedReminderOrThrow(id, user);
        reminderRepository.delete(r);
    }

    @Transactional
    public ReminderResponse setCompleted(Long id, boolean completed, User user) {
        Reminder r = getVisibleReminderOrThrow(id, user);
        if (completed && RecurrenceSupport.isRecurring(r)) {
            r.setCompleted(false);
            r.setIgnoreCount(0);
            r.setLastNotifiedAt(null);
            r.setSnoozedUntil(null);
            r.setScheduledAt(RecurrenceSupport.nextOccurrenceStrictlyAfter(java.time.LocalDateTime.now(), r));
            reminderRepository.save(r);
            return toResponse(r);
        }
        r.setCompleted(completed);
        if (completed) {
            r.setIgnoreCount(0);
            r.setLastNotifiedAt(null);
            r.setSnoozedUntil(null);
        }
        reminderRepository.save(r);
        return toResponse(r);
    }

    @Transactional
    public ReminderResponse setRecurringEnabled(Long id, boolean enabled, User user) {
        Reminder r = getOwnedReminderOrThrow(id, user);
        if (!RecurrenceSupport.isRecurringType(r.getRecurrenceType())) {
            throw new DomainRuleException("Este lembrete não possui repetição configurada");
        }
        r.setRecurringEnabled(enabled);
        if (enabled) {
            r.setCompleted(false);
            r.setScheduledAt(RecurrenceSupport.alignInitialSchedule(
                    r.getScheduledAt(),
                    r.getRecurrenceType(),
                    r.getRecurrenceDays()
            ));
        }
        reminderRepository.save(r);
        return toResponse(r);
    }

    @Transactional
    public ReminderResponse setChewing(Long id, boolean chewing, User user) {
        Reminder r = getVisibleReminderOrThrow(id, user);
        r.setChewing(chewing);
        reminderRepository.save(r);
        return toResponse(r);
    }

    @Transactional
    public ReminderResponse snooze(Long id, int minutes, User user) {
        Reminder r = getVisibleReminderOrThrow(id, user);
        if (r.isCompleted()) {
            throw new DomainRuleException("Lembrete já concluído");
        }
        if (minutes <= 0) {
            throw new DomainRuleException("Minutos de adiamento inválidos");
        }
        r.setSnoozedUntil(java.time.LocalDateTime.now().plusMinutes(minutes));
        reminderRepository.save(r);
        return toResponse(r);
    }

    @Transactional
    public ReminderResponse recordChewingIgnored(Long id, User user) {
        Reminder r = getVisibleReminderOrThrow(id, user);
        if (r.isCompleted()) {
            throw new DomainRuleException("Lembrete já concluído");
        }
        if (!r.isChewing()) {
            throw new DomainRuleException("Modo Chiclete não está ativo para este lembrete");
        }
        r.setIgnoreCount(r.getIgnoreCount() + 1);
        applyPriorityEscalation(r);
        reminderRepository.save(r);
        return toResponse(r);
    }

    @Transactional
    public ReminderResponse shareWithEmail(Long id, String email, User actor) {
        Reminder r = getOwnedReminderOrThrow(id, actor);
        if (email.equalsIgnoreCase(actor.getEmail())) {
            throw new DomainRuleException("Não é possível compartilhar o lembrete com você mesmo");
        }
        User target = userRepository.findByEmail(email)
                .orElseThrow(() -> new DomainRuleException("Usuário não encontrado para compartilhamento"));
        r.getSharedWith().add(target);
        reminderRepository.save(r);
        return toResponse(r);
    }

    private void applyRequest(Reminder r, ReminderRequest request) {
        String recurrenceType = RecurrenceSupport.normalizeType(request.recurrenceType());
        String recurrenceDays = RecurrenceSupport.normalizeDaysForType(recurrenceType, request.recurrenceDays());
        boolean recurringEnabled = request.recurringEnabled() == null || request.recurringEnabled();

        r.setTitle(request.title());
        r.setDescription(request.description());
        r.setChewing(request.chewing());
        r.setIntervalMinutes(request.intervalMinutes());
        r.setPriority(normalizePriority(request.priority()));
        r.setRecurrenceType(recurrenceType);
        r.setRecurrenceDays(recurrenceDays);
        r.setRecurringEnabled(recurringEnabled);

        LocalDateTime scheduledAt = RecurrenceSupport.alignInitialSchedule(
                request.scheduledAt(),
                recurrenceType,
                recurrenceDays
        );
        r.setScheduledAt(scheduledAt);
    }

    private String normalizePriority(String p) {
        return p.trim().toUpperCase(Locale.ROOT);
    }

    private void applyPriorityEscalation(Reminder r) {
        if (ignoreThreshold <= 0) {
            return;
        }
        if (r.getIgnoreCount() > 0 && r.getIgnoreCount() % ignoreThreshold == 0) {
            bumpPriority(r);
        }
    }

    private void bumpPriority(Reminder r) {
        String current = normalizePriority(r.getPriority());
        int idx = PRIORITY_ORDER.indexOf(current);
        if (idx < 0) {
            idx = 0;
        }
        if (idx < PRIORITY_ORDER.size() - 1) {
            r.setPriority(PRIORITY_ORDER.get(idx + 1));
        }
    }

    private boolean canView(Reminder r, User u) {
        if (r.getOwner().getId().equals(u.getId())) {
            return true;
        }
        return r.getSharedWith().stream().anyMatch(s -> s.getId().equals(u.getId()));
    }

    private boolean isOwner(Reminder r, User u) {
        return r.getOwner().getId().equals(u.getId());
    }

    private Reminder getOwnedReminderOrThrow(Long id, User user) {
        Reminder r = reminderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Lembrete não encontrado"));
        if (!isOwner(r, user)) {
            throw new ForbiddenException("Somente o dono pode realizar esta operação");
        }
        return r;
    }

    private Reminder getVisibleReminderOrThrow(Long id, User user) {
        Reminder r = reminderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Lembrete não encontrado"));
        if (!canView(r, user)) {
            throw new NotFoundException("Lembrete não encontrado");
        }
        return r;
    }

    private ReminderResponse toResponse(Reminder r) {
        List<String> sharedEmails = r.getSharedWith().stream()
                .map(User::getEmail)
                .sorted(Comparator.naturalOrder())
                .toList();
        return new ReminderResponse(
                r.getId(),
                r.getTitle(),
                r.getDescription(),
                r.getScheduledAt(),
                r.isChewing(),
                r.getIntervalMinutes(),
                r.getIgnoreCount(),
                r.isCompleted(),
                r.getPriority(),
                r.getRecurrenceType(),
                r.getRecurrenceDays(),
                r.isRecurringEnabled(),
                r.getOwner().getId(),
                sharedEmails
        );
    }
}
