package com.techelevator.tenmo.Server.dao;

import com.techelevator.tenmo.Server.model.Account;

import java.math.BigDecimal;
import java.util.List;

public interface AccountDao {

    public Account createAccount(int userId);

    public Account updateAccount(Account account);

    public Account getAccountById(int id);
    public List<BigDecimal> getBalance(int userId);
    public List<Integer> getAccountIdByUsername(String username);
}
