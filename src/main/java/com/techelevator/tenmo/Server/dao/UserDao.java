package com.techelevator.tenmo.Server.dao;

import com.techelevator.tenmo.Server.model.User;

import java.util.List;

public interface UserDao {

    List<User> findAll();

    List<String> getOtherUsernames(String principal);

    User findByUsername(String username);

    public List<String> getAllUsernames();

    int findIdByUsername(String username);

    boolean create(String username, String password);

    User getUserByAccountId(int acccountId);
}
