package com.backend.organiza.dtos;

import com.backend.organiza.entity.Transaction;

import java.util.List;

public record TransactionResponse(Transaction transaction, List<String> errorMessages) {

}
