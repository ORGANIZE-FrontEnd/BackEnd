package com.backend.organiza.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NonNull;

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

    @NotNull
    private String description;

    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @NonNull
    private String category;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @Nullable
    private LocalDate endDate;

    @NotNull(message = "Recurring flag must be provided")
    private Boolean isRecurring;

    @NotNull(message = "Recurrence type must be specified")
    private String recurrenceType;

    @NotNull(message = "Transaction type must be specified")
    private String transactionType;

    @ManyToOne
    @JsonBackReference
    private User user;

    public Transaction() {
    }

}
