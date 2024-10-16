package com.backend.organiza.controller;

import com.backend.organiza.dtos.LoginResponse;
import com.backend.organiza.dtos.LoginUserDto;
import com.backend.organiza.dtos.TokenDTO;
import com.backend.organiza.dtos.UserRegistrationDTO;
import com.backend.organiza.entity.User;
import com.backend.organiza.service.JwtService;
import com.backend.organiza.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/api/users")
public class UserController {

    private final JwtService jwtService;

    private final UserService userService;

    public UserController(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserDto loginUserDto) {
        User authenticatedUser = userService.authenticate(loginUserDto);

        String accessTokenJwt = jwtService.generateToken(authenticatedUser, JwtService.TokenType.ACCESS_TOKEN, String.valueOf(authenticatedUser.getId()));
        String refreshToken = authenticatedUser.getRefreshToken();
        //validate if creating a new refresh token is necessary
        if(refreshToken != null){
            try {
                jwtService.isTokenExpired(refreshToken);
            } catch (ExpiredJwtException e) {
                refreshToken = jwtService.generateToken(authenticatedUser, JwtService.TokenType.REFRESH_TOKEN, String.valueOf(authenticatedUser.getId()));
            }
        }
        LoginResponse loginResponse = new LoginResponse(
                new TokenDTO(accessTokenJwt, jwtService.getAccessTokenExpiration()),
                new TokenDTO(refreshToken, jwtService.getRefreshTokenExpiration())
        );
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/create")
    public ResponseEntity<User> createUser(@RequestBody UserRegistrationDTO userDTO) {
        User savedUser = userService.createUser(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @GetMapping("/refreshAccessToken/{refreshToken}")
    public ResponseEntity<?> refreshAccessToken(@PathVariable String refreshToken) {

        // Step 1: Validate the refresh token signature and expiration
        if (jwtService.isTokenExpired(refreshToken)) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Expired refresh token expirationTime: {}" + jwtService.getRefreshTokenExpiration());
        }

        // Step 2: Extract the username from the refresh token
        String username = jwtService.extractUsername(refreshToken);

        // Step 3: Load the user from the database by username
        Optional<User> authenticatedUser = userService.getUserByUsername(username);
        if (authenticatedUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        // Step 4: Check if the stored refresh token matches the one provided by the user
        String storedRefreshToken = authenticatedUser.get().getRefreshToken();
        if (!storedRefreshToken.equals(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        }

        // Step 5: Generate a new access token
        String newAccessToken = jwtService.generateToken(authenticatedUser.get(), JwtService.TokenType.ACCESS_TOKEN, String.valueOf(authenticatedUser.get().getId()));

        return ResponseEntity.ok(new TokenDTO(newAccessToken, jwtService.getAccessTokenExpiration()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody String refreshToken) {

        String username = jwtService.extractUsername(refreshToken);

        Optional<User> authenticatedUser = userService.getUserByUsername(username);
        if (authenticatedUser.isEmpty() || !username.equals(authenticatedUser.get().getUsername())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found or doesnt match the auth user");
        }

        if (!authenticatedUser.get().getRefreshToken().equals(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        }

        userService.invalidateRefreshToken(authenticatedUser.get());
        return ResponseEntity.ok("User logged out successfully!");
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable UUID id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable UUID id, @RequestBody User user) {
        User updatedUser = userService.updateUser(id, user);
        return updatedUser != null ? ResponseEntity.ok(updatedUser) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        return userService.deleteUser(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
