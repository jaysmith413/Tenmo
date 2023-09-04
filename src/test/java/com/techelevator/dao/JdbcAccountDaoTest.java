package com.techelevator.dao;

import com.techelevator.tenmo.Server.dao.JdbcAccountDao;
import com.techelevator.tenmo.Server.model.Account;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;

public class JdbcAccountDaoTest extends BaseDaoTests{

    private JdbcAccountDao sut;
    @Before
    public void setUp() throws Exception {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        sut = new JdbcAccountDao(jdbcTemplate);
    }

    @Test
    public void createAccount() {
        Account testAccount = new Account(0, 1002, BigDecimal.valueOf(1000).setScale(2));
        Account actualAccount = sut.createAccount(1002);

        int newId = actualAccount.getAccountId();

        Assert.assertTrue(newId > 0);
        assertAccountsMatchNoId(testAccount,actualAccount);
    }

    @Test
    public void getAccountById() {
        Account retrievedAccount = sut.getAccountById(2001);
        Account testAccount = new Account(2001, 1001, BigDecimal.valueOf(1000).setScale(2));
        assertAccountsMatch(testAccount,retrievedAccount);
    }

    @Test
    public void updateAccount() {
        Account testAccount = new Account(2001, 1001, BigDecimal.valueOf(300).setScale(2));
        Account actualAccount = sut.updateAccount(testAccount);
        assertAccountsMatch(testAccount, actualAccount);
    }

    private void assertAccountsMatchNoId(Account expected, Account actual){
        Assert.assertEquals(expected.getUserId(), actual.getUserId());
        Assert.assertEquals(expected.getBalance(), actual.getBalance());
    }
    private void assertAccountsMatch(Account expected, Account actual){
        Assert.assertEquals(expected.getAccountId(), actual.getAccountId());
        Assert.assertEquals(expected.getUserId(), actual.getUserId());
        Assert.assertEquals(expected.getBalance(), actual.getBalance());
    }
}