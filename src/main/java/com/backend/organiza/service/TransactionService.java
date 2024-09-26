package com.backend.organiza.service;

import com.backend.organiza.entity.Transaction;
import com.backend.organiza.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    public Transaction createTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }


    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public Transaction getTransactionById(UUID id) {
        Optional<Transaction> optionalTransaction = transactionRepository.findById(id);
        return optionalTransaction.orElse(null);
    }

    public Transaction updateTransaction(UUID id, Transaction transaction) {
        if (transactionRepository.existsById(id)) {
            transaction.setId(id); // Ensure the ID is set for the update
            return transactionRepository.save(transaction);
        }
        return null;
    }

    public boolean deleteTransaction(UUID id) {
        if (transactionRepository.existsById(id)) {
            transactionRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
