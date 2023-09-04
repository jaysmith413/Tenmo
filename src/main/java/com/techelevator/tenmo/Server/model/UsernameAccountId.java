package com.techelevator.tenmo.Server.model;

import java.util.ArrayList;
import java.util.List;

public class UsernameAccountId {

    String username;
    List<Integer> accountIds = new ArrayList<>();

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<Integer> getAccountIds() {
        return accountIds;
    }

    public void setAccountIds(List<Integer> accountIds) {
        this.accountIds = accountIds;
    }
}
