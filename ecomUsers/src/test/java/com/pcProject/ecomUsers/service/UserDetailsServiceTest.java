package com.pcProject.ecomUsers.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.pcProject.ecomUsers.model.OrderDetailsWrapper;
import com.pcProject.ecomUsers.model.UserDetails;
import com.pcProject.ecomUsers.model.UserProducts;
import com.pcProject.ecomUsers.model.UserProductsResponse;
import com.pcProject.ecomUsers.repository.UserDetailsRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceTest {

    @Mock
    private UserDetailsRepo userRepo;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private ObjectMapper mockObjectMapper;

    @InjectMocks
    private UserDetailsService userDetailsService;

    private static final String TEST_USER = "test-userName";
    private static final String TEST_PRODUCT = "test-productName";
    private UserDetails existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new UserDetails();
        existingUser.setUserName(TEST_USER);
        existingUser.setUserPassword("test_password");
        existingUser.setUserProductNames(new ArrayList<>(Arrays.asList(TEST_PRODUCT)));
    }

    // --- getAllUserProducts Branch Coverage ---

    @Test
    void getAllUserProducts_WhenUserDoesNotExist_ShouldReturnBadRequestInBody() {
        when(userRepo.findByUserName(TEST_USER)).thenReturn(null);
        ResponseEntity<UserProductsResponse<List<String>>> response = userDetailsService.getAllUserProducts(TEST_USER);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
        assertEquals(0, response.getBody().getData().size());
    }

    @Test
    void getAllUserProducts_WhenUserExists_ShouldReturnProductList() {
        when(userRepo.findByUserName(TEST_USER)).thenReturn(existingUser);
        ResponseEntity<UserProductsResponse<List<String>>> response = userDetailsService.getAllUserProducts(TEST_USER);
        assertEquals(HttpStatus.OK.value(), response.getBody().getStatus());
        assertEquals(1, response.getBody().getData().size());
    }

    // --- kafkaConsumer_AddUserProduct Branch Coverage ---

    @Test
    void kafkaConsumer_AddUserProduct_WhenValidInput_ShouldSaveAndSendKafkaMessage() throws JsonProcessingException, InterruptedException {
        String json = "{\"userName\":\"test-userName\", \"productName\":\"NewProduct\"}";
        OrderDetailsWrapper wrapper = new OrderDetailsWrapper();
        wrapper.setUserName(TEST_USER);
        wrapper.setProductName("NewProduct");

        // Use a real mapper or mock it to return our wrapper
        when(mockObjectMapper.readValue(eq(json), eq(OrderDetailsWrapper.class))).thenReturn(wrapper);
        when(userRepo.findByUserName(TEST_USER)).thenReturn(existingUser);

        userDetailsService.kafkaConsumer_AddUserProduct(json);

        verify(userRepo, times(1)).save(existingUser);
        verify(kafkaTemplate, times(1)).send(eq("ecomUserService_addUserProduct"), any());
        assertEquals(2, existingUser.getUserProductNames().size());
    }

    // --- addUserProduct Branch Coverage ---

    @Test
    void addUserProduct_WhenUserDoesNotExist_ShouldReturnBadRequestInBody() {
        when(userRepo.findByUserName(TEST_USER)).thenReturn(null);
        ResponseEntity<UserProductsResponse<UserProducts>> response = userDetailsService.addUserProduct(TEST_USER, TEST_PRODUCT);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
    }

    @Test
    void addUserProduct_WhenUserExists_ShouldAddProductAndSave() {
        when(userRepo.findByUserName(TEST_USER)).thenReturn(existingUser);
        ResponseEntity<UserProductsResponse<UserProducts>> response = userDetailsService.addUserProduct(TEST_USER, "NewProduct");
        assertEquals(HttpStatus.OK.value(), response.getBody().getStatus());
        verify(userRepo).save(existingUser);
    }

    // --- kafkaConsumer_removeUserProduct Branch Coverage ---

    @Test
    void kafkaConsumer_removeUserProduct_WhenValidInput_ShouldRemoveAndSave() throws JsonProcessingException {
        String json = "{\"userName\":\"test-userName\", \"productName\":\"test-productName\"}";
        OrderDetailsWrapper wrapper = new OrderDetailsWrapper();
        wrapper.setUserName(TEST_USER);
        wrapper.setProductName(TEST_PRODUCT);

        when(mockObjectMapper.readValue(eq(json), eq(OrderDetailsWrapper.class))).thenReturn(wrapper);
        when(userRepo.findByUserName(TEST_USER)).thenReturn(existingUser);

        userDetailsService.kafkaConsumer_removeUserProduct(json);

        assertEquals(0, existingUser.getUserProductNames().size());
        verify(userRepo).save(existingUser);
    }

    // --- removeUserProduct Branch Coverage ---

    @Test
    void removeUserProduct_WhenUserDoesNotExist_ShouldReturnBadRequestInBody() {
        when(userRepo.findByUserName(TEST_USER)).thenReturn(null);
        ResponseEntity<UserProductsResponse<UserProducts>> response = userDetailsService.removeUserProduct(TEST_USER, TEST_PRODUCT);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
    }

    @Test
    void removeUserProduct_WhenProductNotInList_ShouldReturnBadRequestInBody() {
        when(userRepo.findByUserName(TEST_USER)).thenReturn(existingUser);
        ResponseEntity<UserProductsResponse<UserProducts>> response = userDetailsService.removeUserProduct(TEST_USER, "NonExistentProduct");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
        assertEquals("Could not find the Product in Order List", response.getBody().getMessage());
    }

    @Test
    void removeUserProduct_WhenProductInList_ShouldRemoveAndReturnOk() {
        when(userRepo.findByUserName(TEST_USER)).thenReturn(existingUser);
        ResponseEntity<UserProductsResponse<UserProducts>> response = userDetailsService.removeUserProduct(TEST_USER, TEST_PRODUCT);
        assertEquals(HttpStatus.OK.value(), response.getBody().getStatus());
        verify(userRepo).save(existingUser);
    }

    // --- validateUserProduct Branch Coverage ---

    @Test
    void validateUserProduct_WhenUserDoesNotExist_ShouldReturnBadRequestInBody() {
        when(userRepo.findByUserName(TEST_USER)).thenReturn(null);
        ResponseEntity<UserProductsResponse<UserProducts>> response = userDetailsService.validateUserProduct(TEST_USER, TEST_PRODUCT);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
    }

    @Test
    void validateUserProduct_WhenProductDoesNotExistInList_ShouldReturnBadRequestInBody() {
        when(userRepo.findByUserName(TEST_USER)).thenReturn(existingUser);
        ResponseEntity<UserProductsResponse<UserProducts>> response = userDetailsService.validateUserProduct(TEST_USER, "WrongProduct");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
    }

    @Test
    void validateUserProduct_WhenEverythingIsValid_ShouldReturnSuccess() {
        when(userRepo.findByUserName(TEST_USER)).thenReturn(existingUser);
        ResponseEntity<UserProductsResponse<UserProducts>> response = userDetailsService.validateUserProduct(TEST_USER, TEST_PRODUCT);
        assertEquals(HttpStatus.OK.value(), response.getBody().getStatus());
        assertEquals("Validation Successful", response.getBody().getMessage());
    }

    // --- validateUser Branch Coverage ---

    @Test
    void validateUser_WhenUserDoesNotExist_ShouldReturnBadRequest() {
        when(userRepo.findByUserName(TEST_USER)).thenReturn(null);
        ResponseEntity<UserProductsResponse<UserProducts>> response = userDetailsService.validateUser(TEST_USER);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void validateUser_WhenUserExists_ShouldReturnOk() {
        when(userRepo.findByUserName(TEST_USER)).thenReturn(existingUser);
        ResponseEntity<UserProductsResponse<UserProducts>> response = userDetailsService.validateUser(TEST_USER);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // --- getUserProfile Branch Coverage ---

    @Test
    void getUserProfile_WhenUserDoesNotExist_ShouldReturnBadRequest() {
        when(userRepo.findByUserName(TEST_USER)).thenReturn(null);
        Object result = userDetailsService.getUserProfile(TEST_USER);
        assertEquals(HttpStatus.BAD_REQUEST, ((ResponseEntity<?>)result).getStatusCode());
    }

    @Test
    void getUserProfile_WhenUserExists_ShouldReturnUser() {
        when(userRepo.findByUserName(TEST_USER)).thenReturn(existingUser);
        Object result = userDetailsService.getUserProfile(TEST_USER);
        assertEquals(HttpStatus.OK, ((ResponseEntity<?>)result).getStatusCode());
    }

    // --- add/delete/update Branch Coverage ---

    @Test
    void addUserProfile_ShouldSaveAndReturnCreated() {
        ResponseEntity<?> response = (ResponseEntity<?>) userDetailsService.addUserProfile(existingUser);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(userRepo).save(existingUser);
    }

    @Test
    void updateUserProfile_WhenUserDoesNotExist_ShouldReturnBadRequest() {
        when(userRepo.findByUserName(TEST_USER)).thenReturn(null);
        ResponseEntity<?> response = (ResponseEntity<?>) userDetailsService.updateUserProfile(existingUser);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void updateUserProfile_WhenUserExists_ShouldUpdateAndReturnOk() {
        when(userRepo.findByUserName(TEST_USER)).thenReturn(existingUser);
        ResponseEntity<?> response = (ResponseEntity<?>) userDetailsService.updateUserProfile(existingUser);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepo).save(existingUser);
    }

    @Test
    void deleteUserProfile_WhenUserDoesNotExist_ShouldReturnBadRequest() {
        when(userRepo.findByUserName(TEST_USER)).thenReturn(null);
        ResponseEntity<?> response = (ResponseEntity<?>) userDetailsService.deleteUserProfile(TEST_USER);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void deleteUserProfile_WhenUserExists_ShouldDeleteAndReturnOk() {
        when(userRepo.findByUserName(TEST_USER)).thenReturn(existingUser);
        ResponseEntity<?> response = (ResponseEntity<?>) userDetailsService.deleteUserProfile(TEST_USER);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepo).deleteByUserName(TEST_USER);
    }

    @Test
    void getAllUsers_ShouldReturnRepositoryResult() {
        userDetailsService.getAllUsers();
        verify(userRepo).findAll();
    }
}