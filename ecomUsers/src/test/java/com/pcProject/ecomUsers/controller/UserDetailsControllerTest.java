package com.pcProject.ecomUsers.controller;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;

import com.pcProject.ecomUsers.model.UserDetails;
import com.pcProject.ecomUsers.model.UserProducts;
import com.pcProject.ecomUsers.model.UserProductsResponse;
import com.pcProject.ecomUsers.service.UserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(UserDetailsController.class)
public class UserDetailsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserDetailsService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserProducts userProduct;
    private UserDetails userDetails;
    private final String userName = "testUser";
    private final String productName = "testProduct";

    @BeforeEach
    void setUp() {
        userProduct = new UserProducts();
        userProduct.setUserName(userName);
        userProduct.setProductName(productName);

        userDetails = new UserDetails();
        userDetails.setUserName(userName);
    }

    // --- Exposed to OrderService Tests ---

    @Test
    void addUserProduct_ShouldReturnOk() throws Exception {
        UserProductsResponse<UserProducts> response = new UserProductsResponse<>();
        when(userService.addUserProduct(anyString(), anyString()))
                .thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        mockMvc.perform(post("/users/addUserProduct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userProduct)))
                .andExpect(status().isOk());
    }

    @Test
    void getAllUserProducts_ShouldReturnOk() throws Exception {
        UserProductsResponse<List<String>> response = new UserProductsResponse<>();
        when(userService.getAllUserProducts(userName))
                .thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        mockMvc.perform(get("/users/getAllUserProducts/{userName}", userName))
                .andExpect(status().isOk());
    }

    @Test
    void removeUserProduct_ShouldReturnOk() throws Exception {
        UserProductsResponse<UserProducts> response = new UserProductsResponse<>();
        when(userService.removeUserProduct(anyString(), anyString()))
                .thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        mockMvc.perform(post("/users/removeUserProduct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userProduct)))
                .andExpect(status().isOk());
    }

    @Test
    void validateUserProduct_ShouldReturnOk() throws Exception {
        UserProductsResponse<UserProducts> response = new UserProductsResponse<>();
        when(userService.validateUserProduct(anyString(), anyString()))
                .thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        mockMvc.perform(post("/users/validateUserProduct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userProduct)))
                .andExpect(status().isOk());
    }

    @Test
    void validateUser_ShouldReturnOk() throws Exception {
        UserProductsResponse<UserProducts> response = new UserProductsResponse<>();
        when(userService.validateUser(anyString()))
                .thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        mockMvc.perform(post("/users/validateUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userProduct)))
                .andExpect(status().isOk());
    }

    // --- User Profile Management Tests ---

    @Test
    void getUserProfile_ShouldReturnProfile() throws Exception {
        when(userService.getUserProfile(userName))
                .thenReturn(new ResponseEntity<>(userDetails, HttpStatus.OK));

        mockMvc.perform(get("/users/getUserProfile/{username}", userName))
                .andExpect(status().isOk());
    }

    @Test
    void getAllUsers_ShouldReturnList() throws Exception {
        when(userService.getAllUsers())
                .thenReturn(new ResponseEntity<>(Collections.singletonList(userDetails), HttpStatus.OK));

        mockMvc.perform(get("/users/getAllUsers"))
                .andExpect(status().isOk());
    }

    @Test
    void addUserProfile_ShouldReturnCreated() throws Exception {
        when(userService.addUserProfile(any(UserDetails.class)))
                .thenReturn(new ResponseEntity<>(userDetails, HttpStatus.CREATED));

        mockMvc.perform(post("/users/addUserProfile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDetails)))
                .andExpect(status().isCreated());
    }

    @Test
    void updateUserProfile_ShouldReturnOk() throws Exception {
        when(userService.updateUserProfile(any(UserDetails.class)))
                .thenReturn(new ResponseEntity<>(userDetails, HttpStatus.OK));

        mockMvc.perform(put("/users/updateUserProfile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDetails)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteUserProfile_ShouldReturnOk() throws Exception {
        when(userService.deleteUserProfile(userName))
                .thenReturn(new ResponseEntity<>("Deleted successfully", HttpStatus.OK));

        mockMvc.perform(delete("/users/deleteUserProfile/{username}", userName))
                .andExpect(status().isOk());
    }
}