package com.backend.organiza.configuration;


import com.backend.organiza.config.ApplicationConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ApplicationConfigurationTest {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private ApplicationConfiguration applicationConfiguration;

    @Test
    void testUserDetailsService() {
        assertThat(userDetailsService).isNotNull();
    }

    @Test
    void testPasswordEncoder() {
        assertThat(passwordEncoder).isNotNull();
    }

    @Test
    void testAuthenticationManager() {
        assertThat(authenticationManager).isNotNull();
    }
}
