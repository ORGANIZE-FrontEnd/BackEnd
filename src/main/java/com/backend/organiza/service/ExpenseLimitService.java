package com.backend.organiza.service;

import com.backend.organiza.dtos.ExpenseLimitResponse;
import com.backend.organiza.entity.ExpenseLimit;
import com.backend.organiza.repository.ExpenseLimitRepository;
import com.backend.organiza.entity.User;
import com.backend.organiza.dtos.ExpenseLimitDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.backend.organiza.utils.ValidatorUtil.validateCategory;

@Service
public class ExpenseLimitService {

    @Autowired
    private ExpenseLimitRepository expenseLimitRepository;

    @Autowired
    private UserService userService;

    public ExpenseLimitResponse createLimit(UUID userId, ExpenseLimitDTO expenseLimitDTO) {
        List<String> errorMessages = new ArrayList<>();

        validateCategory(expenseLimitDTO.category(), "expense").ifPresent(errorMessages::add);

        if (expenseLimitRepository.existsByUserIdAndCategoryAndMonth(userId, expenseLimitDTO.category(), expenseLimitDTO.month())) {
            errorMessages.add("A limit for this category and month already exists.");
        }

        if (!errorMessages.isEmpty()) {
            return new ExpenseLimitResponse(null, errorMessages);
        }

        ExpenseLimit limit = new ExpenseLimit();
        BeanUtils.copyProperties(expenseLimitDTO, limit);

        Optional<User> user = userService.getUserById(userId);
        if (user.isEmpty()) {
            errorMessages.add("User not found: " + userId);
            return new ExpenseLimitResponse(null, errorMessages);
        }
        limit.setUser(user.get());

        expenseLimitRepository.save(limit);
        return new ExpenseLimitResponse(limit, Collections.emptyList());
    }


    public ExpenseLimitResponse updateExpenseLimit(UUID userId, UUID limitId, ExpenseLimitDTO expenseLimitDTO) {
        List<String> errorMessages = new ArrayList<>();

        validateCategory(expenseLimitDTO.category(), "expense").ifPresent(errorMessages::add);

        if (!errorMessages.isEmpty()) {
            return new ExpenseLimitResponse(null, errorMessages);
        }

        Optional<ExpenseLimit> existingLimitOpt = expenseLimitRepository.findById(limitId);

        if (existingLimitOpt.isEmpty()) {
            errorMessages.add("No limit found with the specified ID.");
            return new ExpenseLimitResponse(null, errorMessages);
        }

        ExpenseLimit existingLimit = existingLimitOpt.get();

        if (!existingLimit.getUser().getId().equals(userId)) {
            errorMessages.add("This limit does not belong to the specified user.");
            return new ExpenseLimitResponse(null, errorMessages);
        }

        existingLimit.setLimitValue(expenseLimitDTO.limitValue());

        expenseLimitRepository.save(existingLimit);

        return new ExpenseLimitResponse(existingLimit, Collections.emptyList());
    }


    public List<ExpenseLimit> getLimitsByUser(UUID userId, LocalDate month) {
        int year = month.getYear();
        int monthValue = month.getMonthValue();
        List<ExpenseLimit> expenseLimits = expenseLimitRepository.findByUserId(userId);

        return expenseLimits.stream()
                .filter(expenseLimit -> {
                    LocalDate limitDate = expenseLimit.getMonth();
                    return limitDate.getYear() == year && limitDate.getMonthValue() == monthValue;
                })
                .collect(Collectors.toList());
    }

    public Optional<ExpenseLimit> getLimitForCategory(UUID userId, String category, LocalDate month) {
        int year = month.getYear();
        int monthValue = month.getMonthValue();
        return expenseLimitRepository.findByUserIdAndCategoryAndYearMonth(userId, category, year, monthValue);
    }
}
