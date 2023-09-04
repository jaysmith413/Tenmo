package com.techelevator.tenmo.Client;

import com.techelevator.tenmo.Client.services.ClientService;
import com.techelevator.tenmo.Server.dao.*;
import com.techelevator.tenmo.Server.model.*;
import org.springframework.data.relational.core.sql.In;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.*;

public class TenmoCLI {

    private static final String MAIN_MENU_OPTION_REGISTER = "Register for a Tenmo Account";
    private static final String MAIN_MENU_OPTION_LOGIN = "Login to your Tenmo Account";
    private static final String MAIN_MENU_OPTION_TRANSFERS = "View and Manage Transfers";
    private static final String MAIN_MENU_OPTION_EXIT = "Exit";
    private static final String [] MAIN_MENU_OPTIONS = new String[] { MAIN_MENU_OPTION_REGISTER,
                                                                      MAIN_MENU_OPTION_LOGIN,
                                                                      MAIN_MENU_OPTION_TRANSFERS,
                                                                      MAIN_MENU_OPTION_EXIT};

    private static final String TRANSFERS_MENU_OPTION_VIEW_USERS = "View Users";
    private static final String TRANSFERS_MENU_OPTION_SEND_TRANSFER = "Send";
    private static final String TRANSFERS_MENU_OPTION_REQUEST_TRANSFER = "Request";
    private static final String TRANSFERS_MENU_OPTION_VIEW_TRANSFERS = "View Transfers";
    private static final String TRANSFERS_MENU_OPTION_EXIT = "Exit";
    private static final String [] TRANSFERS_MENU_OPTIONS = new String[] {TRANSFERS_MENU_OPTION_VIEW_USERS,
                                                                           TRANSFERS_MENU_OPTION_SEND_TRANSFER,
                                                                           TRANSFERS_MENU_OPTION_REQUEST_TRANSFER,
                                                                            TRANSFERS_MENU_OPTION_VIEW_TRANSFERS,
                                                                            TRANSFERS_MENU_OPTION_EXIT};




    private final AccountDao accountDao;
    private final TransferDao transferDao;
    private final UserDao userDao;
    private final Menu menu;
    private final ClientService clientService;

    public static void main(String[] args) {

        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        TenmoCLI application = new TenmoCLI(jdbcTemplate);

        application.run();
    }

    public TenmoCLI(JdbcTemplate jdbcTemplate) {
        this.menu = new Menu(System.in, System.out);

        accountDao = new JdbcAccountDao(jdbcTemplate );
        userDao = new JdbcUserDao(jdbcTemplate, accountDao);
        transferDao = new JdbcTransferDao(jdbcTemplate, userDao, accountDao);
        clientService = new ClientService();

    }

    private void run() {

        boolean running = true;
        while (running) {
            printHeading("Main Menu");
            String choice = (String)menu.getChoiceFromOptions(MAIN_MENU_OPTIONS);
            if (choice.equals(MAIN_MENU_OPTION_REGISTER)) {

                clientService.registerUser(handleResgister());

            } else if (choice.equals(MAIN_MENU_OPTION_LOGIN)) {

                clientService.setAuthToken(clientService.loginUser(handleLogin()));

            } else if (choice.equals(MAIN_MENU_OPTION_TRANSFERS)) {

                transfersMenu();

            } else if (choice.equals(MAIN_MENU_OPTION_EXIT)) {
                running = false;
            }
        }
    }

    private void transfersMenu() {
        boolean isViewTransferMenu = true;
        while (isViewTransferMenu) {

            printHeading("Transfers Menu");
            String transfersChoice = (String) menu.getChoiceFromOptions(TRANSFERS_MENU_OPTIONS);
            if (transfersChoice.equals(TRANSFERS_MENU_OPTION_VIEW_USERS)) {

                printHeading("***User/AccountID***");
                Map<String, List<Integer>> listUA = clientService.userNames();

                for (String eachKey : listUA.keySet()){
                    System.out.println("Username: " + eachKey + " AccountID(s): " + listUA.get(eachKey));
                }

            } else if (transfersChoice.equals(TRANSFERS_MENU_OPTION_SEND_TRANSFER)) {

                DisplayTransfer returnedDT = clientService.createTransfer(handleTransfer());

                printHeading("***Transfer Details***");
                printDisplayTransferWithSend(returnedDT);

            } else if (transfersChoice.equals(TRANSFERS_MENU_OPTION_REQUEST_TRANSFER)) {

                DisplayTransfer returnedDT = clientService.createTransfer(handleRequest());

                printHeading("***Request Details***");
                printDisplayTransferWithRequest(returnedDT);

            } else if (transfersChoice.equals(TRANSFERS_MENU_OPTION_VIEW_TRANSFERS)) {

                //view transfers method
                printHeading("***Transfers***");
                DisplayTransfer[] list = clientService.transferList();

                for(int i = 0; i < list.length; i++) {
                    printDisplayTransferWithoutSend(list[i]);
                }
                DisplayTransfer transfer = clientService.singleTransfer(handleDisplaySingleTransfer());
                printDisplayTransferWithoutSend(transfer);

                if(transfer.getStatus().equals("Pending")){
                    //handler method
                    transfer.setStatus(handleStatusChange());
                    clientService.updateTransfer(transfer);
                    printDisplayTransferWithoutSend(transfer);
                }

            } else if (transfersChoice.equals(TRANSFERS_MENU_OPTION_EXIT)) {

                isViewTransferMenu = false;

            }
        }
    }

    public void printDisplayTransferWithSend(DisplayTransfer displayTransfer){
        System.out.println("Id: " + displayTransfer.getTransferId());
        System.out.println("From: " + displayTransfer.getFrom());
        System.out.println("To: " + displayTransfer.getTo());
        System.out.println("Type: Send");
        System.out.println("Status: " + displayTransfer.getStatus());
        System.out.println("Amount: " + displayTransfer.getTransferAmount());
    }

    public void printDisplayTransferWithRequest(DisplayTransfer displayTransfer){
        System.out.println("Id: " + displayTransfer.getTransferId());
        System.out.println("From: " + displayTransfer.getFrom());
        System.out.println("To: " + displayTransfer.getTo());
        System.out.println("Type: Request");
        System.out.println("Status: " + displayTransfer.getStatus());
        System.out.println("Amount: " + displayTransfer.getTransferAmount());
    }

    public void printDisplayTransferWithoutSend(DisplayTransfer displayTransfer){
        System.out.println("Id: " + displayTransfer.getTransferId());
        System.out.println("From: " + displayTransfer.getFrom());
        System.out.println("To: " + displayTransfer.getTo());
        System.out.println("Status: " + displayTransfer.getStatus());
        System.out.println("Amount: " + displayTransfer.getTransferAmount());
    }
    public Transfer handleTransfer(){
        Transfer newTransfer = new Transfer();
        newTransfer.setFromAccount(Integer.parseInt(getUserInput("Which account are you sending from?")));
        newTransfer.setToAccount(Integer.parseInt(getUserInput("Which account are you sending to?")));
        newTransfer.setTransferAmount(BigDecimal.valueOf(Integer.parseInt(getUserInput("How much would you like to transfer?"))).setScale(2));
        return newTransfer;
    }

    public String handleStatusChange(){
        String status = "";
        boolean toContinue = true;

        while(toContinue) {
            status = getUserInput("Type 1 to approve or type 9 to reject.");
            if (status.equals("1")) {
                status = "Approved";
                toContinue = false;
            } else if (status.equals("9")) {
                status = "Denied";
                toContinue = false;
            }
        }
        return status;
    }

    public int handleDisplaySingleTransfer(){
        int transferId = Integer.parseInt(getUserInput("Please enter transfer ID to view details."));
        return transferId;
    }

    public Transfer handleRequest(){
        Transfer newRequest = new Transfer();
        newRequest.setFromAccount(Integer.parseInt(getUserInput("Which account are you requesting from?")));
        newRequest.setToAccount(Integer.parseInt(getUserInput("In which account do you want the TE bucks?")));
        newRequest.setTransferAmount(BigDecimal.valueOf(Integer.parseInt(getUserInput("How much would you like transferred?"))).setScale(2));
        return newRequest;
    }

    public RegisterUserDTO handleResgister(){
        RegisterUserDTO newUser = new RegisterUserDTO();
        newUser.setUsername(getUserInput("Enter your Username"));
        newUser.setPassword(getUserInput("Enter your Password"));
        return newUser;
    }

    public LoginDTO handleLogin(){
        LoginDTO newLogin = new LoginDTO();
        newLogin.setUsername(getUserInput("Enter your Username"));
        newLogin.setPassword(getUserInput("Enter your Password"));
        return newLogin;
    }


    private void printHeading(String headingText) {
        System.out.println("\n"+headingText);
        for(int i = 0; i < headingText.length(); i++) {
            System.out.print("-");
        }
        System.out.println();
    }

    private String getUserInput(String prompt) {
        System.out.print(prompt + " >>> ");
        return new Scanner(System.in).nextLine();
    }


}


