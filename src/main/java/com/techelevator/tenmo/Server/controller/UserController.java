package com.techelevator.tenmo.Server.controller;

import com.techelevator.tenmo.Server.dao.AccountDao;
import com.techelevator.tenmo.Server.dao.UserDao;
import com.techelevator.tenmo.Server.model.UsernameAccountId;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@PreAuthorize("isAuthenticated()")
@RestController
public class UserController {

    private UserDao userDao;
    private AccountDao accountDao;


    public UserController(UserDao userDao, AccountDao accountDao){
        this.userDao = userDao;
        this.accountDao = accountDao;
    }

    @RequestMapping(path = "/users", method = RequestMethod.GET)
    public Map<String, List<Integer>> list(Principal principal) {
        List<String> usernames = userDao.getAllUsernames();
        Map<String, List<Integer>> listUA = new HashMap<>();

        usernames.remove(principal.getName());
        for (String eachUsername : usernames){
            listUA.put(eachUsername, accountDao.getAccountIdByUsername(eachUsername));
        }

        return listUA;
    }

}
