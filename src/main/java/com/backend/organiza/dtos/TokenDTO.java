package com.backend.organiza.dtos;

public record TokenDTO(String jwt, long expiresIn) {
}
