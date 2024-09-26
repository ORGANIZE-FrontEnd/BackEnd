package com.backend.organiza.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.NotFound;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tb_user")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String name;
    private String email;
    private String phone;
    private LocalDate birthday;
    private String password;
    private List<Transaction> incomeList;
    private List<Transaction> expenseList;
}
