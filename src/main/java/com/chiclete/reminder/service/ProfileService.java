package com.chiclete.reminder.service;

import com.chiclete.reminder.domain.User;
import com.chiclete.reminder.dto.ProfileResponse;
import com.chiclete.reminder.dto.ProfileUpdateRequest;
import com.chiclete.reminder.infra.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final UserRepository userRepository;

    public ProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public ProfileResponse get(User user) {
        return toResponse(user);
    }

    @Transactional
    public ProfileResponse update(User user, ProfileUpdateRequest request) {
        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name().trim());
        }
        if (request.whatsapp() != null) {
            user.setWhatsapp(normalizeWhatsapp(request.whatsapp()));
        }
        if (request.notifyEmail() != null) {
            user.setNotifyEmail(request.notifyEmail());
        }
        if (request.notifyWhatsapp() != null) {
            user.setNotifyWhatsapp(request.notifyWhatsapp());
        }
        if (request.notifyInApp() != null) {
            user.setNotifyInApp(request.notifyInApp());
        }
        userRepository.save(user);
        return toResponse(user);
    }

    static String normalizeWhatsapp(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String digits = raw.replaceAll("\\D", "");
        return digits.isEmpty() ? null : digits;
    }

    static String buildWhatsappLink(String whatsapp, String message) {
        if (whatsapp == null || whatsapp.isBlank() || message == null || message.isBlank()) {
            return null;
        }
        return "https://wa.me/" + whatsapp + "?text=" + java.net.URLEncoder.encode(message, java.nio.charset.StandardCharsets.UTF_8);
    }

    private ProfileResponse toResponse(User user) {
        return new ProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getWhatsapp(),
                user.isNotifyEmail(),
                user.isNotifyWhatsapp(),
                user.isNotifyInApp()
        );
    }
}
