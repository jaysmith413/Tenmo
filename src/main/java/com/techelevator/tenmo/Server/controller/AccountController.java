package com.techelevator.tenmo.Server.controller;

import com.techelevator.tenmo.Server.dao.AccountDao;
import com.techelevator.tenmo.Server.dao.UserDao;
import com.techelevator.tenmo.Server.model.Account;
import com.techelevator.tenmo.Server.model.DisplayBalance;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@PreAuthorize("isAuthenticated()")
@RestController
public class AccountController {
    private AccountDao accountDao;
    private UserDao userDao;

    public AccountController(AccountDao accountDao, UserDao userDao){
        this.accountDao = accountDao;
        this.userDao = userDao;
    }

    @RequestMapping(path = "/balance", method = RequestMethod.GET)
    public List<DisplayBalance> getBalance(Principal principal){
        int userId = userDao.findIdByUsername(principal.getName());
        List<BigDecimal> balances = accountDao.getBalance(userId);

        List<DisplayBalance> displayBalances = new ArrayList<>();
        for(BigDecimal eachBalance : balances){
            DisplayBalance displayBalance = new DisplayBalance();
            displayBalance.setBalance(eachBalance);
            displayBalance.setUsername(principal.getName());
            displayBalances.add(displayBalance);
        }

        return displayBalances;
    }

    @RequestMapping(path = "/newaccount", method = RequestMethod.POST)
    public Account createNewAccount(Principal principal){
        Account newAccount = accountDao.createAccount(userDao.findIdByUsername(principal.getName()));
        return newAccount;
    }

}
