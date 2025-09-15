package com.pcProject.ecomUsers.service;

import com.pcProject.ecomUsers.model.UserDetails;
import com.pcProject.ecomUsers.model.UserProducts;
import com.pcProject.ecomUsers.model.UserProductsResponse;
import com.pcProject.ecomUsers.repository.UserDetailsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsService {

    @Autowired
    private UserDetailsRepo userRepo;


    //Used by OrderService
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

    //Used by OrderService
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
        response.setData(userProducts);
        response.setStatus(HttpStatus.OK.value());
        response.setMessage("Successfully added "+productName + " for "+userName);

        // Need to add productName validation

        List<String> orderedProduct = user.getUserProductNames();
        orderedProduct.add(productName);
        user.setUserProductNames(orderedProduct);
        userRepo.save(user);

        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    //Used by OrderService
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

    //Used by OrderService
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

    public Object getUserProfile(String userName){
        UserDetails user =  userRepo.findByUserName(userName);
        if(user == null)
            return new ResponseEntity<>("Could not find the User with given name",HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(user,HttpStatus.OK);
    }

    public Object addUserProfile(UserDetails user){
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