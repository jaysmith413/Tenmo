package com.techelevator.tenmo.Server.dao;

import com.techelevator.tenmo.Server.exception.DaoException;
import com.techelevator.tenmo.Server.model.DisplayTransfer;
import com.techelevator.tenmo.Server.model.Transfer;
import com.techelevator.tenmo.Server.service.AccountService;
import com.techelevator.tenmo.Server.service.TransferService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTransferDao implements TransferDao{

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;
    private AccountDao accountDao;
    private TransferService transferService = new TransferService();

    private AccountService accountService;


    public JdbcTransferDao (JdbcTemplate jdbcTemplate, UserDao userDao, AccountDao accountDao){
        this.jdbcTemplate = jdbcTemplate;
        this.userDao = userDao;
        this.accountDao = accountDao;
        this.accountService = new AccountService(jdbcTemplate, accountDao);
        }


    @Override
    public List<Transfer> getTransfers() {
        List<Transfer> allTransfers = new ArrayList<>();
        String sql = "SELECT transfer_id, from_account_id, to_account_id, transfer_amount, status FROM transfer;";

        try{
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
            while (results.next()) {
                Transfer transferResult = mapRowToTransfer(results);
                allTransfers.add(transferResult);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return allTransfers;
    }

    @Override
    public Transfer createTransfer(Transfer newTransfer) {

        Transfer returnTransfer = null;
        String sql = "INSERT INTO transfer " +
                     "(from_account_id, to_account_id, transfer_amount, status) " +
                     "VALUES (?, ?, ?, ?) RETURNING transfer_id;";

        try{
        if (userDao.getUserByAccountId(newTransfer.getFromAccount()) == null || userDao.getUserByAccountId(newTransfer.getToAccount()) == null) {
            throw new DaoException("User does not exist");
        }

        // TODO: Add if statement here to account for transfer requests by principal wanting to receive

            Integer newId = this.jdbcTemplate.queryForObject(sql,Integer.class, newTransfer.getFromAccount(),
                     newTransfer.getToAccount(), newTransfer.getTransferAmount(), newTransfer.getStatus());
            returnTransfer = getTransferById(newId);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }

        if (transferService.okToTransfer(returnTransfer, accountDao.getAccountById(returnTransfer.getFromAccount()))) {

            if (returnTransfer.getStatus().equals("Approved")){
                accountService.updateFromBalance(returnTransfer, accountDao.getAccountById(returnTransfer.getFromAccount()));
                accountService.updateToBalance(returnTransfer, accountDao.getAccountById(returnTransfer.getToAccount()));
            }

            return returnTransfer;
        } else {
            throw new DaoException("ERROR: Transfer cannot be created. Not OK to transfer.");
        }
    }

    @Override
    public Transfer updateTransfer(int transferId, Transfer updateTransfer) {
        JdbcAccountDao jdbcAccountDao = new JdbcAccountDao(jdbcTemplate);
        Transfer transfer = null;
        String sql = "UPDATE transfer SET " +
                     "from_account_id=?, to_account_id=?, transfer_amount=?, status=? WHERE transfer_id=?;";

        // TODO: Principal (fromAccount) check through if statement, then allow for status change to approved

        try{
            int numberOfRows = jdbcTemplate.update(sql, updateTransfer.getFromAccount(), updateTransfer.getToAccount(),
                    updateTransfer.getTransferAmount(), updateTransfer.getStatus(), transferId);

            if (numberOfRows == 0){
                throw new DaoException("Zero rows affected, expected at least one");
            } else {
                transfer = getTransferById(transferId);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }

        if (transferService.okToTransfer(transfer, jdbcAccountDao.getAccountById(transfer.getFromAccount()))) {
            if (transfer.getStatus().equals("Approved")){
                accountService.updateFromBalance(transfer, jdbcAccountDao.getAccountById(transfer.getFromAccount()));
                accountService.updateToBalance(transfer, jdbcAccountDao.getAccountById(transfer.getToAccount()));
            }
            return transfer;
        } else {
            throw new DaoException("ERROR: Transfer cannot be updated. Not OK to transfer.");
        }
    }

    private Transfer mapRowToTransfer(SqlRowSet result){
        Transfer transfer = new Transfer();
        transfer.setTransferId(result.getInt("transfer_id"));
        transfer.setFromAccount(result.getInt("from_account_id"));
        transfer.setToAccount(result.getInt("to_account_id"));
        transfer.setTransferAmount(result.getBigDecimal("transfer_amount"));
        transfer.setStatus(result.getString("status"));
        return transfer;
    }

    @Override
    public Transfer getTransferById(int id) {
        Transfer transfer = null;
        String sql = "SELECT transfer_id, from_account_id, to_account_id, transfer_amount, status " +
                     "FROM transfer WHERE transfer_id=?;";

        try{
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql,id);
            if (results.next()){
                transfer = mapRowToTransfer(results);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }

        return transfer;
    }

    @Override
    public DisplayTransfer displayTransfer(int transferId){
        DisplayTransfer displayTransfer = new DisplayTransfer();
        String sql = "SELECT transfer_id, transfer_amount, status FROM transfer WHERE transfer_id = ?;";

        try{
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, transferId);
            if (results.next()){
                displayTransfer.setTransferId(results.getInt("transfer_id"));
                displayTransfer.setTransferAmount(results.getBigDecimal("transfer_amount"));
                displayTransfer.setStatus(results.getString("status"));
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }

        displayTransfer.setFrom(getFromUsername());
        displayTransfer.setTo(getToUsername());
        return displayTransfer;

    }
    private String getFromUsername(){
        String fromUsername = "";
        String sql = "SELECT username FROM tenmo_user JOIN account ON tenmo_user.user_id = account.user_id " +
                "JOIN transfer ON transfer.from_account_id = account.account_id WHERE from_account_id = " +
                "account_id;";

        try{
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
            if(results.next()){
                fromUsername = results.getString("username");
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }

        return fromUsername;
    }
    private String getToUsername(){
        String toUsername = "";
        String sql = "SELECT tenmo_user.username FROM transfer JOIN account ON account.account_id = " +
                "transfer.to_account_id JOIN tenmo_user ON tenmo_user.user_id = account.user_id " +
                "WHERE to_account_id = account_id;";

        try{
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
            if(results.next()){
                toUsername = results.getString("username");
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }
        return toUsername;
    }


    //POST /api/register
    //POST /api/login
    //GET /api/users
    //GET /api/accounts
    //GET /api/accounts/{id} (@PathVariable)
    //GET /api/transfers
    //GET /api/transfers?sent=username (@RequestParam)
    //GET /api/transfers/{id} (@PathVariable)
    //POST /api/transfers
    //PUT /api/transfers/{id} (@PathVariable)

    //build service package with TransferService (business logic)
    //sender exists, sender not blank, sender in JWT of request, receiver exists, receiver not blank, same sender/receiver
    //send amount >= 0.01, send amount: 0.001, insufficient funds

    //Controllers: AccountsController, AuthenticationController, TransfersController, UsersController
}
