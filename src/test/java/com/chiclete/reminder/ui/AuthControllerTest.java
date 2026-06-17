package com.chiclete.reminder.ui;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.chiclete.reminder.service.AuthService;
import com.chiclete.reminder.service.JwtService;
import com.chiclete.reminder.ui.dto.LoginResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;
    @MockBean
    private JwtService jwtService;

    @Test
    void deveRetornar200ComTokenAoFazerLoginValido() throws Exception {
        when(authService.login(any())).thenReturn(new LoginResponse("token123", "Bearer"));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"joao@email.com\",\"password\":\"senha123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("token123"));
    }

    @Test
    void deveRetornar401ComCredenciaisInvalidas() throws Exception {
        when(authService.login(any())).thenThrow(new BadCredentialsException("Credenciais invalidas"));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"joao@email.com\",\"password\":\"errada\"}"))
            .andExpect(status().isUnauthorized());
    }
}
