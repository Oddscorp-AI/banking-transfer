package com.example.banking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.banking.controller.RegistrationController;
import com.example.banking.dto.RegistrationRequest;
import com.example.banking.model.User;
import com.example.banking.service.UserService;

import static org.mockito.Mockito.*;

@WebMvcTest(RegistrationController.class)
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void registerUser() throws Exception {
        String json = "{\"email\":\"test@example.com\",\"password\":\"pass\",\"citizenId\":\"12345\",\"thaiName\":\"Thai\",\"englishName\":\"English\",\"pin\":\"123456\"}";

        User user = new User();
        user.setEmail("test@example.com");
        when(userService.register(any(RegistrationRequest.class))).thenReturn(user);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }
}