package com.techelevator.tenmo.Server.service;

import com.techelevator.tenmo.Server.model.Account;
import com.techelevator.tenmo.Server.model.Transfer;

import java.math.BigDecimal;

public class TransferService {

    public boolean okToTransfer(Transfer transfer, Account fromAccount){
        if (sufficientFunds(transfer,fromAccount) && isNotSelfTransfer(transfer) && isGreaterThanZero(transfer)){
            return true;
        } else {
            return false;
        }
    }

    public boolean sufficientFunds(Transfer transfer, Account fromAccount){
        boolean isSufficient = false;

        if (transfer.getTransferAmount().compareTo(fromAccount.getBalance()) <= 0){
            isSufficient = true;
        }

        return isSufficient;
    }

    public boolean isNotSelfTransfer(Transfer transfer){
        boolean isNotSelf = true;

        if (transfer.getFromAccount() == transfer.getToAccount()){
            isNotSelf = false;
        }

        return isNotSelf;
    }

    public boolean isGreaterThanZero(Transfer transfer){
        boolean isGreaterThanZero = false;

        if (transfer.getTransferAmount().compareTo(BigDecimal.ZERO) == 1){
            isGreaterThanZero = true;
        }
        return isGreaterThanZero;
    }

}
