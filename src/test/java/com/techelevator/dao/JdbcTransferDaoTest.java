package com.techelevator.dao;

import com.techelevator.tenmo.Server.dao.*;
import com.techelevator.tenmo.Server.model.DisplayTransfer;
import com.techelevator.tenmo.Server.model.Transfer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class JdbcTransferDaoTest extends BaseDaoTests{

    private JdbcTransferDao sut;
    private static final Transfer TRANSFER_1 = new Transfer(3001,2001,2002, BigDecimal.valueOf(500.00).setScale(2),"Approved");
    private static final Transfer TRANSFER_2 = new Transfer(3002,2002,2001, BigDecimal.valueOf(500.00).setScale(2),"Pending");

    @Before
    public void setUp() throws Exception {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        AccountDao accountDao = new JdbcAccountDao(jdbcTemplate);
        UserDao userDao = new JdbcUserDao(jdbcTemplate, accountDao);
        sut = new JdbcTransferDao(jdbcTemplate, userDao, accountDao);
    }

    @Test
    public void getTransfers() {
        //Arrange
        List<Transfer> expectedTransfers = new ArrayList<>();
        expectedTransfers.add(TRANSFER_1);
        expectedTransfers.add(TRANSFER_2);


        //Act
        List<Transfer> actualTransfers = sut.getTransfers();

        //Assert
        assertTransfersMatch(expectedTransfers.get(0),actualTransfers.get(0));
        assertTransfersMatch(expectedTransfers.get(1),actualTransfers.get(1));
    }

    @Test
    public void createTransfer() {

        Transfer testTransfer = new Transfer(0,2001,2002,BigDecimal.valueOf(500).setScale(2),"Approved");
        Transfer actualTransfer1 = sut.createTransfer(testTransfer);

        int newId = actualTransfer1.getTransferId();

        Assert.assertTrue(newId > 0);
        assertTransfersMatchNoId(testTransfer,actualTransfer1);
    }

    @Test
    public void updateTransfer() {

        Transfer testTransfer = new Transfer(3002,2002,2001,BigDecimal.valueOf(200).setScale(2),"Approved");
        sut.updateTransfer(testTransfer.getTransferId(), testTransfer);
        Transfer retrievedTransfer = sut.getTransferById(3002);

        assertTransfersMatch(testTransfer,retrievedTransfer);
    }

    @Test
    public void getTransferById() {
        Transfer retrievedTransfer = sut.getTransferById(3001);

        assertTransfersMatch(TRANSFER_1,retrievedTransfer);

    }

    @Test
    public void displayTransfer() {
        DisplayTransfer expected = new DisplayTransfer();
        expected.setTransferId(3001);
        expected.setTransferAmount(BigDecimal.valueOf(500.00).setScale(2));
        expected.setFrom("bob");
        expected.setTo("user");

        DisplayTransfer actual = sut.displayTransfer(3001);

        assertDisplayTransfersMatch(expected, actual);
    }

    private void assertDisplayTransfersMatch(DisplayTransfer expected, DisplayTransfer actual){
        Assert.assertEquals(expected.getTransferId(), actual.getTransferId());
        Assert.assertEquals(expected.getTransferAmount(), actual.getTransferAmount());
        Assert.assertEquals(expected.getTo(), actual.getTo());
        Assert.assertEquals(expected.getFrom(), actual.getFrom());
    }

    private void assertTransfersMatch(Transfer expected, Transfer actual) {
        Assert.assertEquals(expected.getTransferId(), actual.getTransferId());
        Assert.assertEquals(expected.getFromAccount(), actual.getFromAccount());
        Assert.assertEquals(expected.getToAccount(), actual.getToAccount());
        Assert.assertEquals(expected.getTransferAmount(), actual.getTransferAmount());
        Assert.assertEquals(expected.getStatus(), actual.getStatus());
    }

    private void assertTransfersMatchNoId(Transfer expected, Transfer actual) {
        Assert.assertEquals(expected.getFromAccount(), actual.getFromAccount());
        Assert.assertEquals(expected.getToAccount(), actual.getToAccount());
        Assert.assertEquals(expected.getTransferAmount(), actual.getTransferAmount());
        Assert.assertEquals(expected.getStatus(), actual.getStatus());
    }
}
