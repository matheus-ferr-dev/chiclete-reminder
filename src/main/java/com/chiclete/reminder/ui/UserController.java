package com.chiclete.reminder.ui;

import com.chiclete.reminder.domain.User;
import com.chiclete.reminder.service.UserService;
import com.chiclete.reminder.ui.dto.CreateUserRequest;
import com.chiclete.reminder.ui.dto.UserResponse;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<Map<String, String>> help() {
        return ResponseEntity.ok(
            Map.of(
                "message",
                "Registro: envie POST com Content-Type: application/json e corpo: {\"name\",\"email\",\"password\"} (senha mín. 8 caracteres). Não use apenas o endereço no navegador — ele só faz GET.",
                "metodo", "POST",
                "exemploCurl",
                "curl -X POST http://localhost:8080/users -H \"Content-Type: application/json\" -d '{\"name\":\"Fulano\",\"email\":\"a@b.com\",\"password\":\"senha12345\"}'"
            )
        );
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@RequestBody @Valid CreateUserRequest request) {
        User created = userService.register(request);
        UserResponse response = new UserResponse(
            created.getId(),
            created.getName(),
            created.getEmail(),
            created.getRole().name()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
