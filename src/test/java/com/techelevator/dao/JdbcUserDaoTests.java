package com.techelevator.dao;


import com.techelevator.tenmo.Server.dao.AccountDao;
import com.techelevator.tenmo.Server.dao.JdbcAccountDao;
import com.techelevator.tenmo.Server.dao.JdbcUserDao;
import com.techelevator.tenmo.Server.model.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

public class JdbcUserDaoTests extends BaseDaoTests{

    private JdbcUserDao sut;

    @Before
    public void setup() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        AccountDao accountDao = new JdbcAccountDao(jdbcTemplate);
        sut = new JdbcUserDao(jdbcTemplate, accountDao);
    }

    @Test
    public void createNewUser() {
        boolean userCreated = sut.create("TEST_USER","test_password");
        Assert.assertTrue(userCreated);
        User user = sut.findByUsername("TEST_USER");
        Assert.assertEquals("TEST_USER", user.getUsername());
    }


}
