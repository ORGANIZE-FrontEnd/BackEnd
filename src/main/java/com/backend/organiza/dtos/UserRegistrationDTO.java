package com.backend.organiza.dtos;

import java.time.LocalDate;

public record UserRegistrationDTO(String name, String email, String phone, LocalDate birthday, String password) {
}
