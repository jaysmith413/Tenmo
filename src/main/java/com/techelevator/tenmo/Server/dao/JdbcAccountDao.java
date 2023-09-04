package com.techelevator.tenmo.Server.dao;

import com.techelevator.tenmo.Server.exception.DaoException;
import com.techelevator.tenmo.Server.model.Account;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcAccountDao implements AccountDao{

    private JdbcTemplate jdbcTemplate;

    public JdbcAccountDao(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Account createAccount(int userId) {
        Account returnAccount = null;
        BigDecimal initialBalance = BigDecimal.valueOf(1000);
        String sql = "INSERT INTO account " +
                "(user_id, balance) " +
                "VALUES (?, ?) RETURNING account_id;";
        try{
            Integer newId = this.jdbcTemplate.queryForObject(sql,Integer.class, userId, initialBalance);
            returnAccount = getAccountById(newId);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }

        return returnAccount;
    }

    @Override
    public Account getAccountById(int id){
        Account account = null;
        String sql = "SELECT account_id, user_id, balance " +
                "FROM account WHERE account_id = ?;";

        try{
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql,id);
            if (results.next()){
                 account = mapRowToAccount(results);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }

        return account;
    }

    @Override
    public Account updateAccount(Account account) {

        Account updatedAccount = null;
        String sql = "UPDATE account SET " +
                "user_id = ?, balance = ? WHERE account_id=?;";

        try{
            int numberOfRows = this.jdbcTemplate.update(sql, account.getUserId(), account.getBalance(),
                account.getAccountId());

            if (numberOfRows == 0){
                throw new DaoException("Zero rows affected, expected at least one");
            }

            updatedAccount = getAccountById(account.getAccountId());
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }

        return updatedAccount;
    }

    @Override
    public List<BigDecimal> getBalance(int userId){
        BigDecimal balance = null;
        List<BigDecimal> balances = new ArrayList<>();
        String sql = "SELECT balance FROM account JOIN tenmo_user ON account.user_id = tenmo_user.user_id " +
                     "WHERE tenmo_user.user_id = ?;";

        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId);
        while(results.next()){
            balance = results.getBigDecimal("balance");
            balances.add(balance);
        }
        return balances;
    }
    //TODO: getBalance: "SELECT username, balance FROM account JOIN tenmo_user ON account.user_id = tenmo_user.user_id
    //WHERE user_id = ?; (user_id from principal)

    @Override
    public List<Integer> getAccountIdByUsername(String username){
        List<Integer> accountIds = new ArrayList<>();
        int accountId = 0;
        String sql = "SELECT account_id FROM account JOIN tenmo_user ON " +
                     "tenmo_user.user_id = account.user_id WHERE username = ?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, username);
        while (results.next()){
            accountId = results.getInt("account_id");
            accountIds.add(accountId);
        }
        return accountIds;
    }
    private Account mapRowToAccount(SqlRowSet result){
        Account account = new Account();
        account.setAccountId(result.getInt("account_id"));
        account.setUserId(result.getInt("user_id"));
        account.setBalance(result.getBigDecimal("balance"));
        return account;
    }
}
