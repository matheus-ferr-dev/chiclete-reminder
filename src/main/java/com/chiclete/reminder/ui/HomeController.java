package com.chiclete.reminder.ui;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> home() {
        return ResponseEntity.ok(
            Map.of(
                "name", "Chiclete Reminder API",
                "status", "ok",
                "publicEndpoints",
                new String[] {
                    "POST /users  (cadastro — JSON: name, email, password mín. 8 caracteres)",
                    "POST /auth/login  (JSON: email, password)",
                    "GET  /users  (esta página de ajuda)",
                },
                "protected", "Use header Authorization: Bearer <token> em /reminders (GET, POST, PATCH)"
            )
        );
    }
}
