package com.backend.organiza.controller;

import com.backend.organiza.dtos.TransactionDTO;
import com.backend.organiza.dtos.TransactionResponse;
import com.backend.organiza.entity.Transaction;
import com.backend.organiza.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    @Operation(summary = "Create a transaction by user ID", description = "Create a transaction by a user referred by userID")
    @PostMapping("/{userId}")
    public ResponseEntity<?> createTransaction(@RequestBody TransactionDTO transactionDTO,
                                               @PathVariable UUID userId) {
        TransactionResponse response = transactionService.createTransaction(transactionDTO, userId);

        if (response.transaction() != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response.transaction());
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.errorMessages());
        }
    }

    @Operation(summary = "Retrieves the list of incomes", description = "Filter the transactions retrieving only incomes")
    @GetMapping("/incomeList/{userId}")
    public ResponseEntity<Object> getIncomeList(@PathVariable UUID userId) {
        List<Transaction> transactions = transactionService.getIncomeList(userId);
        if(transactions == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("We couldn't find a user with that ID");
        }
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @Operation(summary = "Retrieves the list of user expenses", description = "Filter the transactions retrieving only expenses")
    @GetMapping("/expenseList/{userId}")
    public ResponseEntity<Object> getExpenseList(@PathVariable UUID userId) {
        List<Transaction> transactions = transactionService.getExpenseList(userId);
        if(transactions == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("We couldn't find a user with that ID");
        }
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @Operation(summary = "Retrieves the total income and expense by month/year", description = "Filter the transactions retrieving total expenses and incomes of the specific month/year")
    @GetMapping("/transactionSummary/{userId}/{month}/{year}")
    public ResponseEntity<Object> getTotalIncomeAndExpenseByMonthYear(
            @PathVariable UUID userId,
            @PathVariable int month,
            @PathVariable int year) {

        // Get expenses
        List<Transaction> expenses = transactionService.getExpenseList(userId);
        if (expenses == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("We couldn't find a user with that ID");
        }

        BigDecimal totalExpense = expenses.stream()
                .filter(t -> t.getStartDate().getMonthValue() == month && t.getStartDate().getYear() == year)
                .map(Transaction::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Get incomes
        List<Transaction> incomes = transactionService.getIncomeList(userId);
        if (incomes == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("We couldn't find a user with that ID");
        }

        BigDecimal totalIncome = incomes.stream()
                .filter(t -> t.getStartDate().getMonthValue() == month && t.getStartDate().getYear() == year)
                .map(Transaction::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Prepare response
        Map<String, BigDecimal> response = new HashMap<>();
        response.put("totalExpenses", totalExpense);
        response.put("totalIncomes", totalIncome);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @Operation(summary = "Update the transaction by ID", description = "Update the transaction by id and user id")
    @PutMapping("/{userId}/{transactionId}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable UUID userId,
            @PathVariable UUID transactionId,
            @RequestBody TransactionDTO transactionDTO) {
        TransactionResponse response = transactionService.updateTransaction(userId, transactionId, transactionDTO);

        if (!response.errorMessages().isEmpty()) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }


    @Operation(summary = "Deletes a transaction by ID ")
    @DeleteMapping("/{userId}/{transactionId}")
    public ResponseEntity<TransactionResponse> deleteTransaction(
            @PathVariable UUID userId,
            @PathVariable UUID transactionId) {

        TransactionResponse response = transactionService.deleteTransactionById(userId, transactionId);

        if (!response.errorMessages().isEmpty()) {
            logger.warn("Delete failed with errors: {}", response.errorMessages());
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }
}
