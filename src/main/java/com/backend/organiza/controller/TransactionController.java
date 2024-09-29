package com.backend.organiza.controller;

import com.backend.organiza.dtos.TransactionDTO;
import com.backend.organiza.dtos.TransactionResponse;
import com.backend.organiza.entity.Transaction;
import com.backend.organiza.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

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

    @GetMapping("/incomeList/{userId}")
    public ResponseEntity<Object> getIncomeList(@PathVariable UUID userId) {
        List<Transaction> transactions = transactionService.getIncomeList(userId);
        if(transactions == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("We couldn't find a user with that ID");
        }
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @GetMapping("/expenseList/{userId}")
    public ResponseEntity<Object> getExpenseList(@PathVariable UUID userId) {
        List<Transaction> transactions = transactionService.getExpenseList(userId);
        if(transactions == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("We couldn't find a user with that ID");
        }
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

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
