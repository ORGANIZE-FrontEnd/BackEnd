package com.backend.organiza.controller;

import com.backend.organiza.dtos.ExpenseLimitDTO;
import com.backend.organiza.dtos.ExpenseLimitResponse;
import com.backend.organiza.entity.ExpenseLimit;
import com.backend.organiza.service.ExpenseLimitService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/expenseLimits/{userId}")
public class ExpenseLimitController {

    @Autowired
    private ExpenseLimitService expenseLimitService;

    @Operation(summary = "Create an expenseLimit by user ID and category", description = "Create a expense limit for a specific month/year and category")
    @PostMapping()
    public ResponseEntity<ExpenseLimitResponse> createExpenseLimit(
            @PathVariable UUID userId,
            @RequestBody ExpenseLimitDTO expenseLimitDTO) {

        ExpenseLimitResponse response = expenseLimitService.createLimit(userId, expenseLimitDTO);
        if (!response.errorMessages().isEmpty()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update an expenseLimit by user ID and limitId")
    @PutMapping("/{limitId}")
    public ResponseEntity<ExpenseLimitResponse> updateExpenseLimit(
            @PathVariable UUID userId,
            @PathVariable UUID limitId,
            @RequestBody ExpenseLimitDTO expenseLimitDTO) {

        ExpenseLimitResponse response = expenseLimitService.updateExpenseLimit(userId, limitId, expenseLimitDTO);
        if (!response.errorMessages().isEmpty()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "Retrieves the list of limits By user ID")
    @GetMapping
    public ResponseEntity<List<ExpenseLimit>> getLimitsForUser(@PathVariable UUID userId) {
        List<ExpenseLimit> limits = expenseLimitService.getLimitsByUser(userId);
        return ResponseEntity.ok(limits);
    }


    @Operation(summary = "Retrieves the list of limits by id and category")
    @GetMapping("/category")
    public ResponseEntity<ExpenseLimit> getLimitForCategory(
            @PathVariable UUID userId,
            @RequestParam String category,
            @RequestParam String month) {

        LocalDate parsedMonth = LocalDate.parse(month + "-01");
        Optional<ExpenseLimit> limit = expenseLimitService.getLimitForCategory(userId, category, parsedMonth);
        return limit.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
