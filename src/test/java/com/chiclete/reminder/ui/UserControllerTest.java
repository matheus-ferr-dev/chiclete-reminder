package com.chiclete.reminder.ui;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.chiclete.reminder.domain.User;
import com.chiclete.reminder.domain.UserRole;
import com.chiclete.reminder.service.JwtService;
import com.chiclete.reminder.service.UserService;
import com.chiclete.reminder.service.exception.EmailAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;
    @MockBean
    private JwtService jwtService;

    @Test
    void deveRetornar201AoCadastrarUsuarioValido() throws Exception {
        User user = new User();
        user.setName("Joao");
        user.setEmail("joao@email.com");
        user.setRole(UserRole.COMUM);
        when(userService.register(any())).thenReturn(user);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Joao\",\"email\":\"joao@email.com\",\"password\":\"senha123\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("joao@email.com"));
    }

    @Test
    void deveRetornar400QuandoEmailInvalido() throws Exception {
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Joao\",\"email\":\"invalido\",\"password\":\"senha123\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar400QuandoSenhaMenorQue8Chars() throws Exception {
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Joao\",\"email\":\"joao@email.com\",\"password\":\"123\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar409QuandoEmailJaCadastrado() throws Exception {
        when(userService.register(any())).thenThrow(new EmailAlreadyExistsException("Email ja cadastrado."));

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Joao\",\"email\":\"joao@email.com\",\"password\":\"senha123\"}"))
            .andExpect(status().isConflict());
    }
}
