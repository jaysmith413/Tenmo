package com.techelevator.tenmo.Server.controller;


import com.techelevator.tenmo.Server.dao.AccountDao;
import com.techelevator.tenmo.Server.dao.TransferDao;
import com.techelevator.tenmo.Server.dao.UserDao;
import com.techelevator.tenmo.Server.model.DisplayTransfer;
import com.techelevator.tenmo.Server.model.Transfer;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@PreAuthorize("isAuthenticated()")
@RestController
public class TransferController {

    private TransferDao transferDao;
    private UserDao userDao;
    private AccountDao accountDao;


    public TransferController(TransferDao transferDao, UserDao userDao, AccountDao accountDao){
        this.transferDao = transferDao;
        this.userDao = userDao;
        this.accountDao = accountDao;
    }

    @RequestMapping(path = "/transfers", method = RequestMethod.GET)
    public DisplayTransfer[] getTransfersByUser(Principal principal){
        List<Transfer> allTransfers = new ArrayList<>();
        List<Transfer> selectTransfers = new ArrayList<>();
        List<DisplayTransfer> selectDisplayTransfers = new ArrayList<>();
        //get all transfers
        allTransfers = transferDao.getTransfers();
        for(Transfer eachTransfer: allTransfers){
            String fromUsername = userDao.getUserByAccountId(eachTransfer.getFromAccount()).getUsername();
            String toUsername = userDao.getUserByAccountId(eachTransfer.getToAccount()).getUsername();
            if(fromUsername.equals(principal.getName()) || toUsername.equals(principal.getName())){
                selectTransfers.add(eachTransfer);
                DisplayTransfer displayTransfer = createDisplayTransfer(eachTransfer, fromUsername, toUsername);
                selectDisplayTransfers.add(displayTransfer);
            }

        }

        DisplayTransfer[] list = new DisplayTransfer[selectDisplayTransfers.size()];
        for(int i = 0; i < list.length; i++){
            list[i] = selectDisplayTransfers.get(i);
        }
        return list;

    }
    public DisplayTransfer createDisplayTransfer(Transfer transfer, String fromUsername, String toUsername){
        DisplayTransfer displayTransfer = new DisplayTransfer();
        displayTransfer.setTransferId(transfer.getTransferId());
        displayTransfer.setTransferAmount(transfer.getTransferAmount());
        displayTransfer.setTo(toUsername);
        displayTransfer.setFrom(fromUsername);
        displayTransfer.setStatus(transfer.getStatus());
        return displayTransfer;
    }

    @RequestMapping(path = "/transfers", method = RequestMethod.POST)
    public DisplayTransfer createTransfer(@RequestBody Transfer transfer, Principal principal){
        boolean isPrincipalSender = false;
        DisplayTransfer newDisplayTransfer = new DisplayTransfer();
        Transfer newTransfer = new Transfer();


        if (principal.getName().equals(userDao.getUserByAccountId(transfer.getFromAccount()).getUsername())){
            isPrincipalSender = true;
        }

        if (isPrincipalSender == true) {

            transfer.setStatus("Approved");
            //getAccountIdByUsername
            newTransfer = transferDao.createTransfer(transfer);

        } else {

            transfer.setStatus("Pending");
            //getAccountIdByUsername
            newTransfer = transferDao.createTransfer(transfer);
        }

        newDisplayTransfer.setStatus(newTransfer.getStatus());
        newDisplayTransfer.setFrom(userDao.getUserByAccountId(newTransfer.getFromAccount()).getUsername());
        newDisplayTransfer.setTo(userDao.getUserByAccountId(newTransfer.getToAccount()).getUsername());
        newDisplayTransfer.setTransferAmount(newTransfer.getTransferAmount());
        newDisplayTransfer.setTransferId(newTransfer.getTransferId());
        return newDisplayTransfer;
    }

    @RequestMapping(path = "/transfers/{id}", method = RequestMethod.PUT)
    public DisplayTransfer updateTransferStatusById(@PathVariable int id, @RequestBody Transfer transfer, Principal principal){

        Transfer transferToUpdate = new Transfer();

        String fromUsername = userDao.getUserByAccountId(transferDao.getTransferById(id).getFromAccount()).getUsername();

        if(fromUsername.equals(principal.getName()) && !transferDao.getTransferById(id).getStatus().equals("Approved")) {

            transferToUpdate = transferDao.getTransferById(id);
            transferToUpdate.setStatus(transfer.getStatus());
            transferDao.updateTransfer(id, transferToUpdate);

        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "ERROR: User not authorized");
        }
        DisplayTransfer displayTransfer = transferDao.displayTransfer(transferToUpdate.getTransferId());
        return displayTransfer;

    }

    @RequestMapping(path = "/transfers/{id}", method = RequestMethod.GET)
    public DisplayTransfer getTransferById(@PathVariable int id, Principal principal){

        Transfer returnTransfer = transferDao.getTransferById(id);
        String fromUsername = userDao.getUserByAccountId(returnTransfer.getFromAccount()).getUsername();
        String toUsername = userDao.getUserByAccountId(returnTransfer.getToAccount()).getUsername();

        if (!fromUsername.equals(principal.getName()) && !toUsername.equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "ERROR: User not authorized");
        }

        DisplayTransfer displayTransfer = createDisplayTransfer(returnTransfer, fromUsername, toUsername);
        return displayTransfer;
    }

}
