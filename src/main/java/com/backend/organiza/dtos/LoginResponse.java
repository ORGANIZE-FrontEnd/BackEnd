package com.backend.organiza.dtos;

public record LoginResponse(String token, long expiresIn) {
}
