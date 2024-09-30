package com.backend.organiza.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseLimitDTO(BigDecimal limitValue, String category, LocalDate month) {
}
