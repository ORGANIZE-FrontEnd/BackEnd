package com.backend.organiza.repository;

import com.backend.organiza.entity.ExpenseLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ExpenseLimitRepository extends JpaRepository<ExpenseLimit, UUID> {
}
