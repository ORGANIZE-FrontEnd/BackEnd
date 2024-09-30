package com.backend.organiza.dtos;

import com.backend.organiza.entity.ExpenseLimit;

import java.util.List;

public record ExpenseLimitResponse(ExpenseLimit expenseLimit, List<String> errorMessages){
}
