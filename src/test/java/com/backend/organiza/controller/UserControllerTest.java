package com.backend.organiza.controller;

import com.backend.organiza.controller.UserController;
import com.backend.organiza.dtos.UserRegistrationDTO;
import com.backend.organiza.entity.User;
import com.backend.organiza.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UserRegistrationDTO userDTO;

    @BeforeEach
    public void setup() {
        // Mock the user data for the test
        userDTO = new UserRegistrationDTO("John Doe","test@example.com",null,null,"password123");
    }

    @Test
    public void testCreateUser_Success() throws Exception {
        // Mock the behavior of the UserService to return a User when createUser is called
        User mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail("test@example.com");
        mockUser.setName("John Doe");

        when(userService.createUser(userDTO)).thenReturn(mockUser);

        // Perform the request and check the response
        mockMvc.perform(post("/api/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\", \"username\":\"testuser\", \"password\":\"password123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    public void testCreateUser_Failure_UserExists() throws Exception {
        // Simulate a failure when the user already exists
        when(userService.createUser(userDTO)).thenReturn(null);  // Simulating user already exists

        mockMvc.perform(post("/api/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\", \"username\":\"testuser\", \"password\":\"password123\"}"))
                .andExpect(status().isBadRequest())  // We expect a 400 if user already exists
                .andExpect(content().string("User with this email already exists."));
    }
}
