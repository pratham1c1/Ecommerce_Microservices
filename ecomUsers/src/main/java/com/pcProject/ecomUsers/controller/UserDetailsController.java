package com.pcProject.ecomUsers.controller;

import com.pcProject.ecomUsers.model.UserDetails;
import com.pcProject.ecomUsers.model.UserProducts;
import com.pcProject.ecomUsers.model.UserProductsResponse;
import com.pcProject.ecomUsers.service.UserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("users")
public class UserDetailsController {

    @Autowired
    private UserDetailsService userService;

//    -----------------------------------------
    // Exposed to OrderService
    @PostMapping("addUserProduct")
    public ResponseEntity<UserProductsResponse<UserProducts>> addUserProduct(@RequestBody UserProducts userProduct){
        return userService.addUserProduct(userProduct.getUserName(),userProduct.getProductName());
    }
    // Exposed to OrderService
    @GetMapping("getAllUserProducts/{userName}")
    public ResponseEntity<UserProductsResponse<List<String>>> getAllUserProducts(@PathVariable String userName){
        return userService.getAllUserProducts(userName);
    }

    // Exposed to OrderService
    @PostMapping("removeUserProduct")
    public ResponseEntity<UserProductsResponse<UserProducts>> removeUserProduct(@RequestBody UserProducts userProduct){
        return userService.removeUserProduct(userProduct.getUserName(),userProduct.getProductName());
    }

    // Exposed to OrderService
    @PostMapping("validateUserProduct")
    public ResponseEntity<UserProductsResponse<UserProducts>> validateUserProduct(@RequestBody UserProducts userProduct){
        return userService.validateUserProduct(userProduct.getUserName(),userProduct.getProductName());
    }

    //Exposed to OrderService
    @PostMapping("validateUser")
    public ResponseEntity<UserProductsResponse<UserProducts>> validateUser(@RequestBody UserProducts userProduct){
        return userService.validateUser(userProduct.getUserName());
    }

//    -------------------------------------


    @GetMapping("getUserProfile/{username}")
    public Object getUserProfile(@PathVariable String username){
        return userService.getUserProfile(username);
    }

    @GetMapping("getAllUsers")
    public Object getAllUsers(){
        return userService.getAllUsers();
    }

    @PostMapping("addUserProfile")
    public Object addUserProfile(@RequestBody UserDetails user){
        return userService.addUserProfile(user);
    }

    @PutMapping("updateUserProfile")
    public Object updateUserProfile(@RequestBody UserDetails user){
        return userService.updateUserProfile(user);
    }

    @DeleteMapping("deleteUserProfile/{username}")
    public Object deleteUserProfile(@PathVariable String username){
        return userService.deleteUserProfile(username);
    }
}