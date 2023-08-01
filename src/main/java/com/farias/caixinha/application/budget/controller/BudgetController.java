package com.farias.caixinha.application.budget.controller;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.farias.caixinha.application.budget.model.Budget;
import com.farias.caixinha.application.budget.service.BudgetService;
import com.farias.caixinha.common.CrudController;

import lombok.Getter;
import lombok.Setter;

@RestController
@RequestMapping("/budget")
public class BudgetController extends CrudController<Budget, Budget> {

    @Autowired
    BudgetService budgetService;

    @PostMapping("/{id}/withdrawn")
    public ResponseEntity<Budget> withdrawn(@PathVariable("id") UUID id, @RequestBody WithdrawnRequest request) {
        request.validate();
        var persisted = budgetService.withdrawn(id, request.getAmount());
        return ResponseEntity.ok(persisted);
    }

    @PostMapping("/{id}/reset")
    public ResponseEntity<String> resetBalance(@PathVariable("id") UUID id) {
        budgetService.resetBalance(id);
        return ResponseEntity.ok("reseted");
    }

    @Override
    public Class<Budget> getEntityClass() {
        return Budget.class;
    }

    @Override
    public Class<Budget> getDTOClass() {
        return Budget.class;
    }

}

@Getter
@Setter
class WithdrawnRequest {
    BigDecimal amount;

    void validate() {
        if (amount == null || this.amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be greater than zero");
        }
    }
}
