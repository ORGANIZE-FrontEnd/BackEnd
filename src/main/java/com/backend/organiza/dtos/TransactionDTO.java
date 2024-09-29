package com.backend.organiza.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionDTO(
        String description,
        BigDecimal price,
        String category,
        LocalDate startDate,
        LocalDate endDate,
        Boolean isRecurring,
        String recurrenceType,
        String transactionType
) {
}
