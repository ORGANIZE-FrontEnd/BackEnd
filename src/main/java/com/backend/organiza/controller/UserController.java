package com.backend.organiza.controller;

import com.backend.organiza.dtos.LoginResponse;
import com.backend.organiza.dtos.LoginUserDto;
import com.backend.organiza.dtos.TokenDTO;
import com.backend.organiza.dtos.UserRegistrationDTO;
import com.backend.organiza.entity.User;
import com.backend.organiza.service.CookieServiceImpl;
import com.backend.organiza.service.JwtService;
import com.backend.organiza.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

import static com.backend.organiza.service.CookieServiceImpl.eraseCookie;

@RestController
@CrossOrigin(origins = "https://organiza-frontend.netlify.app", allowCredentials = "true")
@RequestMapping("/api/users")
public class UserController {

    private final JwtService jwtService;

    private final UserService userService;

    private final CookieServiceImpl cookieService;

    public UserController(JwtService jwtService, UserService userService, CookieServiceImpl cookieService) {
        this.jwtService = jwtService;
        this.userService = userService;
        this.cookieService = cookieService;
    }

    @Operation(summary = "Login the user in ", description = "Creates a session cookie and an Access Token")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserDto loginUserDto, HttpServletResponse response) {
        User authenticatedUser = userService.authenticate(loginUserDto);

        String accessTokenJwt = jwtService.generateToken(authenticatedUser, JwtService.TokenType.ACCESS_TOKEN, String.valueOf(authenticatedUser.getId()));
        String refreshToken = jwtService.generateToken(authenticatedUser, JwtService.TokenType.REFRESH_TOKEN, String.valueOf(authenticatedUser.getId()));

        cookieService.saveEncryptedToken(response, refreshToken);
        LoginResponse loginResponse = new LoginResponse(
                new TokenDTO(accessTokenJwt, jwtService.getAccessTokenExpiration()));

        return ResponseEntity.ok(loginResponse);
    }

    @Operation(summary = "Creates a user", description = "the user pass is encrypted using a secret key from jwtService")
    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody UserRegistrationDTO userDTO) {
        User savedUser = userService.createUser(userDTO);

        if(savedUser == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User with this email already exist.");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @Operation(summary = "Refresh the user Access Token", description = "Validate if the uses is present && refresh the access token and cookies if successfully")
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {

        String refreshToken = cookieService.getDecryptedToken(request);

        if (refreshToken == null || jwtService.isTokenExpired(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("token invalid or expired");
        }

        try {

            String userName = jwtService.extractUsername(refreshToken);
            Optional<User> user = userService.getUserByUsername(userName);

            if(user.isPresent()){
                String newAccessToken = jwtService.generateToken(user.get(), JwtService.TokenType.ACCESS_TOKEN, String.valueOf(user.get().getId()));

                String newRefreshToken = jwtService.generateToken(user.get(), JwtService.TokenType.REFRESH_TOKEN, String.valueOf(user.get().getId()));
                cookieService.saveEncryptedToken(response, newRefreshToken);
                return ResponseEntity.status(HttpStatus.CREATED).body(new TokenDTO(newAccessToken, jwtService.getAccessTokenExpiration()));
            }
        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return null;
    }


    @PostMapping("/logout")
    @Operation(summary = "Invalidates user session", description = "Erases user session cookie if present")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        if(eraseCookie(cookieService.getCookieName(), response)){
            return ResponseEntity.ok("User logged out successfully!");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User session invalid or not found.");
    }


    @Operation(summary = "Get a user by ID", description = "Fetch a user by their unique ID")
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable UUID id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update a user by ID", description = "Update a user by their unique ID")
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable UUID id, @RequestBody User user) {
        User updatedUser = userService.updateUser(id, user);
        return updatedUser != null ? ResponseEntity.ok(updatedUser) : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Delete a user by ID", description = "Delete a user by their unique ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        return userService.deleteUser(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
