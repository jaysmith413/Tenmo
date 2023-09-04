package com.techelevator.tenmo.Server.model;

import java.math.BigDecimal;

public class Transfer {

    private int transferId;
    private int fromAccount;
    private int toAccount;
    private BigDecimal transferAmount;
    private String status;

    public Transfer() {
    }

    public Transfer(int transferId, int fromAccount, int toAccount, BigDecimal transferAmount, String status) {
        this.transferId = transferId;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.transferAmount = transferAmount;
        this.status = status;
    }

    public int getTransferId() {
        return transferId;
    }

    public void setTransferId(int transferId) {
        this.transferId = transferId;
    }

    public int getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(int fromAccount) {
        this.fromAccount = fromAccount;
    }

    public int getToAccount() {
        return toAccount;
    }

    public void setToAccount(int toAccount) {
        this.toAccount = toAccount;
    }

    public BigDecimal getTransferAmount() {
        return transferAmount;
    }

    public void setTransferAmount(BigDecimal transferAmount) {
        this.transferAmount = transferAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
