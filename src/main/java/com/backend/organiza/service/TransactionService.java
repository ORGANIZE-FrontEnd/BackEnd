package com.backend.organiza.service;

import com.backend.organiza.dtos.TransactionDTO;
import com.backend.organiza.dtos.TransactionResponse;
import com.backend.organiza.entity.Transaction;
import com.backend.organiza.entity.User;
import com.backend.organiza.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.backend.organiza.utils.ValidatorUtil.*;

@Service
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserService userService;

    public TransactionResponse createTransaction(TransactionDTO transactionDTO, UUID userId) {
        List<String> errorMessages = new ArrayList<>();

        Optional<User> user = userService.getUserById(userId);
        if (user.isEmpty()) {
            errorMessages.add("User not found: " + userId);
            return new TransactionResponse(null, errorMessages);
        }

        errorMessages.addAll(validateTransaction(transactionDTO));

        if (!errorMessages.isEmpty()) {
            return new TransactionResponse(null, errorMessages);
        }

        Transaction transaction = new Transaction();
        BeanUtils.copyProperties(transactionDTO, transaction);
        transaction.setUser(user.get());
        user.get().getTransactions().add(transaction);

        transactionRepository.save(transaction);
        return new TransactionResponse(transaction, Collections.emptyList());
    }

    public List<Transaction> getTransactions(UUID userId) {
        return userService.getUserById(userId)
                .map(User::getTransactions)
                .orElse(Collections.emptyList());
    }

    public TransactionResponse updateTransaction(UUID userId, UUID transactionId, TransactionDTO transactionDTO) {
        List<String> errorMessages = new ArrayList<>();

        Optional<User> user = userService.getUserById(userId);
        if (user.isEmpty()) {
            errorMessages.add("User not found: " + userId);
            return new TransactionResponse(null, errorMessages);
        }

        errorMessages.addAll(validateTransaction(transactionDTO));

        if (!errorMessages.isEmpty()) {
            return new TransactionResponse(null, errorMessages);
        }

        Transaction existingTransaction = getTransactionIfBelongsToUser(user.get(), transactionId, errorMessages);
        if (existingTransaction == null) {
            return new TransactionResponse(null, errorMessages);
        }

        BeanUtils.copyProperties(transactionDTO, existingTransaction, "id");
        transactionRepository.save(existingTransaction);
        return new TransactionResponse(existingTransaction, Collections.emptyList());
    }

    public TransactionResponse deleteTransactionById(UUID userId, UUID transactionId) {
        List<String> errorMessages = new ArrayList<>();

        // Fetch user and validate
        Optional<User> user = userService.getUserById(userId);
        if (user.isEmpty()) {
            errorMessages.add("User not found: " + userId);
            return new TransactionResponse(null, errorMessages);
        }

        Transaction transaction = getTransactionIfBelongsToUser(user.get(), transactionId, errorMessages);
        if (transaction == null) {
            return new TransactionResponse(null, errorMessages);
        }

        user.get().getTransactions().remove(transaction);
        transactionRepository.delete(transaction);
        return new TransactionResponse(transaction, Collections.emptyList());
    }

    public List<Transaction> getIncomeList(UUID userId) {
        return getFilteredTransactions(userId, "income");
    }

    public List<Transaction> getExpenseList(UUID userId) {
        return getFilteredTransactions(userId, "expense");
    }

    // Validations
    private List<String> validateTransaction(TransactionDTO transactionDTO) {
        List<String> errorMessages = new ArrayList<>();

        validateTransactionType(transactionDTO.transactionType()).ifPresent(errorMessages::add);
        validateRecurrenceType(transactionDTO.recurrenceType()).ifPresent(errorMessages::add);
        validateCategory(transactionDTO.category(), transactionDTO.transactionType()).ifPresent(errorMessages::add);

        return errorMessages;
    }

    private Transaction getTransactionIfBelongsToUser(User user, UUID transactionId, List<String> errorMessages) {
        Optional<Transaction> optionalTransaction = transactionRepository.findById(transactionId);
        if (optionalTransaction.isEmpty()) {
            errorMessages.add("Transaction not found for ID: " + transactionId);
            return null;
        }

        Transaction transaction = optionalTransaction.get();
        if (!user.getTransactions().contains(transaction)) {
            errorMessages.add("Transaction does not belong to the user: " + user.getId());
            return null;
        }
        return transaction;
    }

    private List<Transaction> getFilteredTransactions(UUID userId, String transactionType) {
        return userService.getUserById(userId)
                .map(user -> user.getTransactions().stream()
                        .filter(transaction -> transactionType.equalsIgnoreCase(transaction.getTransactionType()))
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

}
