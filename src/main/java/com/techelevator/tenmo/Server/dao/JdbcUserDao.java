package com.techelevator.tenmo.Server.dao;

import com.techelevator.tenmo.Server.exception.DaoException;
import com.techelevator.tenmo.Server.model.User;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcUserDao implements UserDao {

    private JdbcTemplate jdbcTemplate;

    //ADDED
    private AccountDao accountDao;

    //ADDED to dependency injection
    public JdbcUserDao(JdbcTemplate jdbcTemplate, AccountDao accountDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.accountDao = accountDao;
    }

    @Override
    public int findIdByUsername(String username) {
        String sql = "SELECT user_id FROM tenmo_user WHERE username ILIKE ?;";
        Integer id = jdbcTemplate.queryForObject(sql, Integer.class, username);
        if (id != null) {
            return id;
        } else {
            return -1;
        }
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, username, password_hash FROM tenmo_user;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
        while(results.next()) {
            User user = mapRowToUser(results);
            users.add(user);
        }
        return users;
    }

    @Override
    public List<String> getAllUsernames(){
        List<String> usernames = new ArrayList<>();
        String sql = "SELECT username FROM tenmo_user;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
        while(results.next()){
            String username = results.getString("username");
            usernames.add(username);
        }
        return usernames;
    }

    //TODO: List<String> getOtherUsernames: "SELECT username FROM tenmo_user WHERE username != ?; (principal)

    @Override
    public List<String> getOtherUsernames(String principal) {
        List<String> otherUsernames = new ArrayList<>();
        String sql = "SELECT username FROM tenmo_user WHERE username != ?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, principal);
        while(results.next()){
            User user = new User();
            user.setUsername(results.getString("username"));
            String username = user.getUsername();
            otherUsernames.add(username);
        }
        return otherUsernames;
    }

    @Override
    public User findByUsername(String username) throws UsernameNotFoundException {
        String sql = "SELECT user_id, username, password_hash FROM tenmo_user WHERE username ILIKE ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, username);
        if (rowSet.next()){
            return mapRowToUser(rowSet);
        }
        throw new UsernameNotFoundException("User " + username + " was not found.");
    }

    @Override
    public boolean create(String username, String password) {

        // create user
        String sql = "INSERT INTO tenmo_user (username, password_hash) VALUES (?, ?) RETURNING user_id";
        String password_hash = new BCryptPasswordEncoder().encode(password);
        Integer newUserId;
        try {
            newUserId = jdbcTemplate.queryForObject(sql, Integer.class, username, password_hash);
        } catch (DataAccessException e) {
            return false;
        }

        // TODO: Create the account record with initial balance

        this.accountDao.createAccount(newUserId);

        return true;
    }

    @Override
    public User getUserByAccountId(int accountId){
        User newUser = null;
        String sql = "SELECT u.user_id, username, password_hash " +
                "FROM tenmo_user u " +
                "JOIN account a ON u.user_id = a.user_id " +
                "WHERE a.account_id = ?;";

        try {

            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, accountId);
            if (results.next()) {
                newUser = mapRowToUser(results);
            }
        } catch	(CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }

        return newUser;
    }

    private User mapRowToUser(SqlRowSet rs) {
        User user = new User();
        user.setId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password_hash"));
        user.setActivated(true);
        user.setAuthorities("USER");
        return user;
    }
}
