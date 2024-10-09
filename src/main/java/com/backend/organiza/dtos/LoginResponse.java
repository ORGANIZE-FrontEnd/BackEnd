package com.backend.organiza.dtos;

public record LoginResponse(TokenDTO accessToken, TokenDTO refreshToken) {
}

