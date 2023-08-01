package com.farias.caixinha.application.budget.model;

import java.math.BigDecimal;
import java.util.UUID;

import com.farias.caixinha.common.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Budget implements BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String name;
    private String icon;
    private String theme;
    private BigDecimal initialBalance;
    private BigDecimal balance;

}
