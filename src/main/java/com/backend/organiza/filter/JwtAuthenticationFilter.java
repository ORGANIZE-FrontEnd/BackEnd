package com.backend.organiza.filter;

import com.backend.organiza.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final HandlerExceptionResolver handlerExceptionResolver;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    private static final Logger logger = Logger.getLogger(JwtAuthenticationFilter.class.getName());

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserDetailsService userDetailsService,
            HandlerExceptionResolver handlerExceptionResolver
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String jwt = request.getHeader("X-ORGANIZA-JWT");
        logger.info(() -> "Request URI: " + request.getRequestURI());

        if (jwt == null || jwt.isEmpty()) {
            logger.warning("JWT token is missing or empty");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (isTokenExpiredOrInvalid(jwt)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid or expired JWT token");
                return;
            }

            String userEmail = jwtService.extractUsername(jwt);
            logger.info(() -> "Extracted username: " + userEmail);

            Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();

            if (userEmail != null && currentAuth == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    logger.info(() -> "Authentication set for user: " + userEmail);
                }
            }

            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error during JWT processing", ex);
            handlerExceptionResolver.resolveException(request, response, null, ex);
        }
    }

    private boolean isTokenExpiredOrInvalid(String jwt) {
        try {
            return jwtService.isTokenExpired(jwt);
        } catch (ExpiredJwtException ex) {
            logger.warning("JWT expired: " + ex.getMessage());
            return true;
        } catch (MalformedJwtException ex) {
            logger.warning("Malformed JWT token: " + ex.getMessage());
            return true;
        } catch (Exception ex) {
            logger.warning("Invalid JWT token: " + ex.getMessage());
            return true;
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        AntPathMatcher pathMatcher = new AntPathMatcher();

        return pathMatcher.match("/api/users/refresh-token", path) ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-ui") ||
                path.equals("/swagger-ui.html") ||
                path.equals("/api/ping");
    }
}
