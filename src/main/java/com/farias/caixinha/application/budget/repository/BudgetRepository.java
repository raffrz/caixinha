package com.farias.caixinha.application.budget.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.farias.caixinha.application.budget.model.Budget;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, UUID> {

}
