package com.backend.organiza.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Data
@Table(name= "tb_expenseLimit")
public class ExpenseLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private BigDecimal limitValue;

    private String category;

    private LocalDate limitDate;
}
