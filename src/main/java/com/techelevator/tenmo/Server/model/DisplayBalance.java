package com.techelevator.tenmo.Server.model;

import java.math.BigDecimal;

public class DisplayBalance {
    private String username;
    private BigDecimal balance;


    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
