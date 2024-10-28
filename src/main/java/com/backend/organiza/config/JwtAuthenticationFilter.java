package com.backend.organiza.config;

import com.backend.organiza.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
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
        final String jwt = request.getHeader("X-ORGANIZA-JWT");
        logger.info("Received request to: " + request.getRequestURI());

        if (jwt == null) {
            logger.warning("JWT token is missing in the request header");
            filterChain.doFilter(request, response);
            return;
        }

        if (isTokenExpiredOrInvalid(jwt, response)) {
            return;
        }



        try {
            final String userEmail = jwtService.extractUsername(jwt);
            logger.info("Extracted username from JWT: " + userEmail);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (userEmail != null && authentication == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                logger.info("Loaded UserDetails for: " + userEmail);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    logger.info("JWT is valid for user: " + userEmail);
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception exception) {
            logger.severe("Exception occurred during JWT authentication: " + exception.getMessage());
            handlerExceptionResolver.resolveException(request, response, null, exception);
        }
    }

    private boolean isTokenExpiredOrInvalid(String jwt, HttpServletResponse response) throws IOException {
        try {
            if (jwtService.isTokenExpired(jwt)) {
                logger.warning("JWT is expired");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("JWT expired");
                return true;
            }
        } catch (ExpiredJwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("JWT expired");
            return true;
        }
        return false;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        AntPathMatcher pathMatcher = new AntPathMatcher();

        // Define paths to bypass filtering
        return pathMatcher.match("/api/users/refresh-token", path) ||
                pathMatcher.match("/v3/api-docs/**", path) ||  // OpenAPI JSON docs
                pathMatcher.match("/swagger-ui/**", path) ||   // Swagger UI resources
                pathMatcher.match("/swagger-ui.html", path);   // Swagger UI HTML page
    }

}


