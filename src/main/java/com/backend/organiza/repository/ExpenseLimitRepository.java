package com.backend.organiza.repository;

import com.backend.organiza.entity.ExpenseLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExpenseLimitRepository extends JpaRepository<ExpenseLimit, UUID> {

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END " +
            "FROM ExpenseLimit e WHERE e.user.id = :userId " +
            "AND e.category = :category AND e.month = :month")
    boolean existsByUserIdAndCategoryAndMonth(@Param("userId") UUID userId,
                                              @Param("category") String category,
                                              @Param("month") LocalDate month);



    @Query("SELECT e FROM ExpenseLimit e WHERE e.user.id = :userId AND e.category = :category AND YEAR(e.month) = :year AND MONTH(e.month) = :month")
    Optional<ExpenseLimit> findByUserIdAndCategoryAndYearMonth(
            @Param("userId") UUID userId,
            @Param("category") String category,
            @Param("year") int year,
            @Param("month") int month
    );

    List<ExpenseLimit> findByUserId(UUID userId);
}
