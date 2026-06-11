package com.chiclete.reminder.ui;

import com.chiclete.reminder.dto.ProfileResponse;
import com.chiclete.reminder.dto.ProfileUpdateRequest;
import com.chiclete.reminder.service.CurrentUserService;
import com.chiclete.reminder.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final CurrentUserService currentUserService;

    public ProfileController(ProfileService profileService, CurrentUserService currentUserService) {
        this.profileService = profileService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ProfileResponse get() {
        return profileService.get(currentUserService.requireCurrentUser());
    }

    @PutMapping
    public ProfileResponse update(@Valid @RequestBody ProfileUpdateRequest request) {
        return profileService.update(currentUserService.requireCurrentUser(), request);
    }
}
