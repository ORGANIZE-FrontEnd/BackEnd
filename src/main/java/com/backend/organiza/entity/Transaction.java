package com.backend.organiza.entity;

import com.backend.organiza.enums.RecurrenceType;
import com.backend.organiza.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Data
@Table(name = "tb_transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private BigDecimal price;

    private String category;

    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean isRecurring;

    @Enumerated(EnumType.STRING)
    private RecurrenceType recurrenceType;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;
}
