package com.techelevator.tenmo.Client.services;

import com.techelevator.tenmo.Server.model.*;
import org.apache.coyote.Response;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientService {

    private static final String API_BASE_URL = "http://localhost:8080";
    private final RestTemplate restTemplate = new RestTemplate();

    private String authToken = null;

    public void setAuthToken(String authToken){
        this.authToken = authToken;
    }


    public RegisterUserDTO registerUser(RegisterUserDTO newUser){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RegisterUserDTO> entity = new HttpEntity<>(newUser,headers);

        RegisterUserDTO returnedUser = null;
        try {
        returnedUser = restTemplate.postForObject(API_BASE_URL + "/register", entity, RegisterUserDTO.class);
        } catch (RestClientResponseException | ResourceAccessException e) {
            System.out.println(e.getMessage());
        }

        return returnedUser;
    }

    public String loginUser(LoginDTO newLogin){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginDTO> entity = new HttpEntity<>(newLogin,headers);

        String token = null;
        try {
            ResponseEntity<TokenDTO> response = restTemplate.exchange(API_BASE_URL + "/login", HttpMethod.POST, entity, TokenDTO.class);
            TokenDTO body = response.getBody();

            if (body != null) {
                token = body.getToken();
            }
        } catch (RestClientResponseException | ResourceAccessException e) {
            System.out.println(e.getMessage());
        }

        return token;
    }

    public Map<String, List<Integer>> userNames(){

        Map<String, List<Integer>> returnList = new HashMap<>();
        try {
            ResponseEntity<Map> response = restTemplate.exchange(API_BASE_URL + "/users", HttpMethod.GET, makeAuthEntity(), Map.class);
            returnList = response.getBody();

        } catch (RestClientResponseException | ResourceAccessException e) {
            System.out.println(e.getMessage());
        }

        return returnList;
    }

    public DisplayTransfer[] transferList(){


        try {
            ResponseEntity<DisplayTransfer[]> response = restTemplate.exchange(API_BASE_URL + "/transfers", HttpMethod.GET, makeAuthEntity(), DisplayTransfer[].class);
            DisplayTransfer[] listOfTransfers = response.getBody();
            return listOfTransfers;

        } catch (RestClientResponseException | ResourceAccessException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public DisplayTransfer singleTransfer(int transferId){
        DisplayTransfer transfer = new DisplayTransfer();
        try{
            ResponseEntity<DisplayTransfer> response = restTemplate.exchange(API_BASE_URL + "/transfers/" + transferId, HttpMethod.GET, makeAuthEntity(), DisplayTransfer.class);
            transfer = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            System.out.println(e.getMessage());
        }
        return transfer;
    }

    public DisplayTransfer createTransfer(Transfer newTransfer){

        HttpEntity<Transfer> entity = makeTransfersEntity(newTransfer);

        DisplayTransfer returnedDisplayTransfer = null;
        try {
            returnedDisplayTransfer = restTemplate.postForObject(API_BASE_URL + "/transfers", entity, DisplayTransfer.class);
        } catch (RestClientResponseException | ResourceAccessException e) {
            System.out.println(e.getMessage());
        }

        return returnedDisplayTransfer;
    }
    public boolean updateTransfer(DisplayTransfer displayTransfer){
        boolean success = false;
        try {
            restTemplate.exchange(API_BASE_URL + "transfers/" + displayTransfer.getTransferId(), HttpMethod.PUT,
                    makeDisplayTransferEntity(displayTransfer), DisplayTransfer.class);
            success = true;

        } catch (RestClientResponseException | ResourceAccessException e) {
            System.out.println(e.getMessage());
        }
        return success;
    }

    private HttpEntity<Transfer> makeTransfersEntity(Transfer transfer) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(transfer, headers);
    }

    private HttpEntity<DisplayTransfer> makeDisplayTransferEntity(DisplayTransfer displayTransfer) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(displayTransfer, headers);
    }



    private HttpEntity<Void> makeAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(headers);
    }

}
