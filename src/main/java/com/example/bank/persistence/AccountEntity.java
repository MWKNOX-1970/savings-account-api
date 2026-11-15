package com.example.bank.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Persistence model only. It never leaves the persistence/action boundary. */
@Entity
@Table(name = "savings_accounts")
public class AccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", nullable = false, updatable = false, length = 12)
    private String accountNumber;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "account_nick_name", length = 30)
    private String accountNickName;

    protected AccountEntity() {
        // required by JPA
    }

    public AccountEntity(String accountNumber, String customerName, String accountNickName) {
        this.accountNumber = accountNumber;
        this.customerName = customerName;
        this.accountNickName = accountNickName;
    }

    public Long getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getAccountNickName() {
        return accountNickName;
    }
}
