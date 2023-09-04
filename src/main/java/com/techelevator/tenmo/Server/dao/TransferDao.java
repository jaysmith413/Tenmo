package com.techelevator.tenmo.Server.dao;

import com.techelevator.tenmo.Server.model.DisplayTransfer;
import com.techelevator.tenmo.Server.model.Transfer;

import java.util.List;

public interface TransferDao {

    public List<Transfer> getTransfers();

    public Transfer createTransfer(Transfer newTransfer);

    public Transfer updateTransfer(int transferId, Transfer updateTransfer);

    public DisplayTransfer displayTransfer(int transferId);

    public Transfer getTransferById(int id);

}
