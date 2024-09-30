package com.backend.organiza.utils;

import java.util.List;
import java.util.Optional;

public class ValidatorUtil {

    public static Optional<String> validateCategory(String category, String transactionType) {
        List<String> validCategories = transactionType.equals("income") ?
                List.of("Emprestimos", "Investimentos", "Salario", "Outras receitas") :
                List.of("Alimentação", "Transporte", "Saúde", "Educação", "Lazer", "Outros");

        return validCategories.contains(category) ? Optional.empty() :
                Optional.of("Invalid " + transactionType + " category: " + category);
    }
    public static Optional<String> validateRecurrenceType(String recurrenceType) {
        List<String> validRecurrenceTypes = List.of("Semanal", "Quinzenal", "Mensal", "Trimestral", "Anual");
        return validRecurrenceTypes.contains(recurrenceType) ? Optional.empty() :
                Optional.of("Invalid recurrence type: " + recurrenceType);
    }

    public static Optional<String> validateTransactionType(String transactionType) {
        List<String> validTransactionTypes = List.of("income", "expense");
        return validTransactionTypes.stream().anyMatch(validType -> validType.equalsIgnoreCase(transactionType)) ?
                Optional.empty() : Optional.of("Invalid transaction type: " + transactionType);
    }
}
