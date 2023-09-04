package com.techelevator.tenmo.Server.service;

import com.techelevator.tenmo.Server.dao.AccountDao;
import com.techelevator.tenmo.Server.model.Account;
import com.techelevator.tenmo.Server.model.Transfer;
import org.springframework.jdbc.core.JdbcTemplate;

public class AccountService {

    private JdbcTemplate jdbcTemplate;
    private AccountDao accountDao;

    public AccountService(JdbcTemplate jdbcTemplate, AccountDao accountDao){
        this.jdbcTemplate = jdbcTemplate;
        this.accountDao = accountDao;
    }

    public void updateToBalance (Transfer transfer, Account toAccount){
        toAccount.setBalance(toAccount.getBalance().add(transfer.getTransferAmount()));
        accountDao.updateAccount(toAccount);
    }

    public void updateFromBalance (Transfer transfer, Account fromAccount) {
        fromAccount.setBalance(fromAccount.getBalance().subtract(transfer.getTransferAmount()));
        accountDao.updateAccount(fromAccount);
    }


}
