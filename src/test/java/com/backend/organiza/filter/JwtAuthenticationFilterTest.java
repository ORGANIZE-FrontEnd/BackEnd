package com.backend.organiza.filter;


import com.backend.organiza.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.io.PrintWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HandlerExceptionResolver handlerExceptionResolver;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String VALID_JWT = "valid-jwt-token";
    private static final String INVALID_JWT = "invalid-jwt-token";
    private static final String EXPIRED_JWT = "expired-jwt-token";
    private static final String USER_EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testDoFilterInternal_missingToken() throws ServletException, IOException {
        // Test for missing JWT token
        when(request.getHeader("X-ORGANIZA-JWT")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);  // Ensure the filter chain is continued
    }

    @Test
    void testDoFilterInternal_invalidToken() throws ServletException, IOException {
        // Test for invalid JWT token
        when(request.getHeader("X-ORGANIZA-JWT")).thenReturn(INVALID_JWT);
        when(jwtService.isTokenExpired(INVALID_JWT)).thenReturn(true);

        // Mocking HttpServletResponse to return a PrintWriter
        PrintWriter printWriter = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(printWriter);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Verify the response status and the message being written
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(printWriter).write("Invalid or expired JWT token");
    }


    @Test
    void testDoFilterInternal_validToken() throws ServletException, IOException {
        // Test for valid JWT token
        when(request.getHeader("X-ORGANIZA-JWT")).thenReturn(VALID_JWT);
        when(jwtService.isTokenExpired(VALID_JWT)).thenReturn(false);
        when(jwtService.isTokenValid(eq(VALID_JWT), any(UserDetails.class))).thenReturn(true);

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername(USER_EMAIL)).thenReturn(userDetails);
        when(jwtService.extractUsername(VALID_JWT)).thenReturn(USER_EMAIL);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_expiredToken() throws ServletException, IOException {
        // Test for expired JWT token
        PrintWriter printWriter = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(printWriter);
        when(request.getHeader("X-ORGANIZA-JWT")).thenReturn(EXPIRED_JWT);
        when(jwtService.isTokenExpired(EXPIRED_JWT)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response.getWriter()).write("Invalid or expired JWT token");
    }

    @Test
    void testShouldNotFilter() throws ServletException {
        // Test paths that should be excluded from the filter
        when(request.getRequestURI()).thenReturn("/api/users/refresh-token");
        assertThat(jwtAuthenticationFilter.shouldNotFilter(request)).isTrue();

        when(request.getRequestURI()).thenReturn("/swagger-ui");
        assertThat(jwtAuthenticationFilter.shouldNotFilter(request)).isTrue();

        when(request.getRequestURI()).thenReturn("/api/ping");
        assertThat(jwtAuthenticationFilter.shouldNotFilter(request)).isTrue();
    }
}
