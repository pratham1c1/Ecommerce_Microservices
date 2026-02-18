package com.pcProject.ecomUsers.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pcProject.ecomUsers.model.OrderDetailsWrapper;
import com.pcProject.ecomUsers.model.UserDetails;
import com.pcProject.ecomUsers.model.UserProducts;
import com.pcProject.ecomUsers.model.UserProductsResponse;
import com.pcProject.ecomUsers.repository.UserDetailsRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class UserDetailsService {

    @Autowired
    private UserDetailsRepo userRepo;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private KafkaTemplate<String,Object> kafkaTemplate;

    //Exposed to OrderService
    // To get all Products of a user
    public ResponseEntity<UserProductsResponse<List<String>>> getAllUserProducts(String userName) {
        UserDetails existingUser = userRepo.findByUserName(userName);
        UserProductsResponse response = new UserProductsResponse();

        if(existingUser == null){
            response.setMessage("Could not find the User with given name");
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setData(new ArrayList<>());
            return new ResponseEntity<>(response,HttpStatus.OK);
        }
        response.setMessage("Success");
        response.setStatus(HttpStatus.OK.value());
        response.setData(existingUser.getUserProductNames());
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @Transactional
    @KafkaListener(topics = "ecomOrderService_addUserProduct")
    public void kafkaConsumer_AddUserProduct(String userProduct) throws JsonProcessingException, InterruptedException {
        log.info("Received ecomOrderService addUserProduct request : {}",userProduct);
//        Thread.sleep(600);
        OrderDetailsWrapper userProducts = objectMapper.readValue(userProduct,OrderDetailsWrapper.class);
        UserDetails user =  userRepo.findByUserName(userProducts.getUserName());

        log.info("Adding the details to the userList");
        // Add the product details in userList
        List<String> orderedProduct = user.getUserProductNames();
        orderedProduct.add(userProducts.getProductName());
        user.setUserProductNames(orderedProduct);
        userRepo.save(user);
        log.info("Added the {} to userList for {} user",userProducts.getProductName(),userProducts.getUserName());

        UserProductsResponse<OrderDetailsWrapper> userProductsResponse = new UserProductsResponse<>();
        userProductsResponse.setData(userProducts);
        userProductsResponse.setMessage("Successfully Added "+userProducts.getUserName()+" details");
        userProductsResponse.setStatus(HttpStatus.OK.value());

        log.info("Sending the message to ecomOrderService for placing an Order {}", userProductsResponse);
        kafkaTemplate.send("ecomUserService_addUserProduct" , userProductsResponse);
    }

    //Exposed to OrderService where we are already doing userName and productName validation
    public ResponseEntity<UserProductsResponse<UserProducts>> addUserProduct(String userName, String productName){
        UserDetails user =  userRepo.findByUserName(userName);
        UserProductsResponse<UserProducts> response = new UserProductsResponse<>();
        UserProducts userProducts = new UserProducts(userName,productName);

        if(user == null) {
            response.setData(null);
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMessage("Could not find the User with given name");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        // Need to add productName validation to make this method independent

        List<String> orderedProduct = user.getUserProductNames();
        orderedProduct.add(productName);
        user.setUserProductNames(orderedProduct);
        userRepo.save(user);

        response.setData(userProducts);
        response.setStatus(HttpStatus.OK.value());
        response.setMessage("Successfully added "+ productName + " for "+userName);

        return new ResponseEntity<>(response,HttpStatus.OK);
    }


    @Transactional
    @KafkaListener(topics="ecomOrderService_removeUserProduct")
    public void kafkaConsumer_removeUserProduct(String orderDetails) throws JsonProcessingException {
        log.info("Received ecomOrderService remove UserProduct request : {}",orderDetails);

        OrderDetailsWrapper orderDetailsWrapper = objectMapper.readValue(orderDetails, OrderDetailsWrapper.class);
        UserDetails userDetails = userRepo.findByUserName(orderDetailsWrapper.getUserName());
        List<String> userProducts = userDetails.getUserProductNames();
        log.info("Fetched User Details : {} -> {}",userDetails.getUserName(),userProducts);

        userProducts.remove(orderDetailsWrapper.getProductName());
        userDetails.setUserProductNames(userProducts);
        userRepo.save(userDetails);
        log.info("Saved the updated list successfully : {}",userDetails.getUserProductNames());

    }

    //Exposed to OrderService
    public ResponseEntity<UserProductsResponse<UserProducts>> removeUserProduct(String userName, String productName) {
        UserDetails user =  userRepo.findByUserName(userName);
        UserProductsResponse<UserProducts> response = new UserProductsResponse<>();
        UserProducts userProducts = new UserProducts(userName,productName);

        if(user == null) {
            response.setData(null);
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMessage("Could not find the User with given name");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        response.setData(userProducts);
        response.setStatus(HttpStatus.OK.value());
        response.setMessage("Successfully removed "+ productName + " for "+userName);



        // Need to add productName valiadation using ProductService

        List<String> orderedProduct = user.getUserProductNames();

        // Validate if Product exists in Order List
        if(!orderedProduct.contains(productName)){
            response.setData(null);
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMessage("Could not find the Product in Order List");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        orderedProduct.remove(productName);
        user.setUserProductNames(orderedProduct);
        userRepo.save(user);

        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    //Exposed to OrderService
    public ResponseEntity<UserProductsResponse<UserProducts>> validateUserProduct(String userName, String productName) {
        UserDetails user =  userRepo.findByUserName(userName);
        UserProductsResponse<UserProducts> response = new UserProductsResponse<>();

        if(user == null) {
            response.setData(null);
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMessage("Could not find the User with given name");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        else if(!user.getUserProductNames().contains(productName)){
            response.setData(null);
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMessage("Could not find the Product in User Order list");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        response.setData(new UserProducts(userName,productName));
        response.setStatus(HttpStatus.OK.value());
        response.setMessage("Validation Successful");
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    // Exposed to OrderService
    public ResponseEntity<UserProductsResponse<UserProducts>> validateUser(String userName) {
        UserDetails existingUser = userRepo.findByUserName(userName);
        ObjectMapper objectMapper = new ObjectMapper();
        UserProductsResponse<UserProducts> response = new UserProductsResponse<>();

        if(existingUser == null){
            response.setMessage("Could not find the User with given name");
            response.setData(null);
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
        }

        response.setData(new UserProducts(userName,null));
        response.setMessage("User Details are valid ");
        response.setStatus(HttpStatus.OK.value());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public Object getUserProfile(String userName){
        UserDetails user =  userRepo.findByUserName(userName);
        if(user == null)
            return new ResponseEntity<>("Could not find the User with given name",HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(user,HttpStatus.OK);
    }

    public Object addUserProfile(UserDetails user){
        user.setUserPassword(new BCryptPasswordEncoder().encode(user.getUserPassword()));
        userRepo.save(user);
        return new ResponseEntity<>(user,HttpStatus.CREATED);
    }

    public Object updateUserProfile(UserDetails user){
        UserDetails existingUser = userRepo.findByUserName(user.getUserName());
        if(existingUser == null)
            return new ResponseEntity<>("Could not find the User with given name",HttpStatus.BAD_REQUEST);

        existingUser.setUserEmail(user.getUserEmail());
        existingUser.setUserPassword(user.getUserPassword());
        existingUser.setUserMobileNumber(user.getUserMobileNumber());
        userRepo.save(existingUser);
        return new ResponseEntity<>(existingUser,HttpStatus.OK);
    }

    public Object deleteUserProfile(String userName){
        UserDetails existingUser = userRepo.findByUserName(userName);
        if(existingUser == null)
            return new ResponseEntity<>("Could not find the User with given name",HttpStatus.BAD_REQUEST);

        userRepo.deleteByUserName(userName);
        return new ResponseEntity<>("User Profile deleted successfully", HttpStatus.OK);
    }

    public Object getAllUsers() {
        return userRepo.findAll();
    }


}