package com.farias.caixinha.application.budget.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.farias.caixinha.application.budget.model.Budget;
import com.farias.caixinha.common.CrudService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;

@Service
public class BudgetService extends CrudService<Budget> {

    @Autowired
    EntityManager entityManager;

    @Transactional
    public Budget withdrawn(UUID budgetId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be a positive value.");
        }
        var budget = repository.findById(budgetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Budget with ID " + budgetId + " not found."));
        entityManager.lock(budget, LockModeType.PESSIMISTIC_WRITE);
        var newBalance = budget.getBalance().subtract(amount);
        budget.setBalance(newBalance);
        return repository.save(budget);
    }

    public void resetBalance(UUID budgetId) {
        var budget = repository.findById(budgetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Budget with ID " + budgetId + " not found."));
        entityManager.lock(budget, LockModeType.PESSIMISTIC_WRITE);
        var newBalance = budget.getInitialBalance();
        budget.setBalance(newBalance);
        repository.save(budget);
    }

}
