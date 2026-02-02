package com.pcProject.ecomOrderService.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pcProject.ecomOrderService.model.UserProducts;
import com.pcProject.ecomOrderService.model.UserProductsResponse;
import com.pcProject.ecomOrderService.service.PaymentDetailsService;
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

@WebMvcTest(PaymentDetailsController.class)
public class PaymentDetailsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentDetailsService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TEST_USER = "test-userName";
    private static final String TEST_PRODUCT = "test-productName";
    private static final String BASE_URL = "/payment";

    @Test
    void getAllPayment_ShouldReturnTotalAmount() throws Exception {
        // Arrange
        UserProductsResponse<String> serviceResponse = new UserProductsResponse<>("Total: $500.00", 200, "Success");

        when(paymentService.getAllPayment(anyString()))
                .thenReturn(new ResponseEntity<>(serviceResponse, HttpStatus.OK));

        String expectedJson = objectMapper.writeValueAsString(serviceResponse);

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/totalPayment/" + TEST_USER))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson, true));
    }

    @Test
    void OneProductPayment_ShouldReturnPaymentConfirmation() throws Exception {
        // Arrange
        UserProducts userProduct = new UserProducts(TEST_USER, TEST_PRODUCT);
        UserProductsResponse<UserProducts> serviceResponse = new UserProductsResponse<>(userProduct, 200, "Payment Processed");

        when(paymentService.OneProductPayment(any(UserProducts.class)))
                .thenReturn(new ResponseEntity<>(serviceResponse, HttpStatus.OK));

        String requestJson = objectMapper.writeValueAsString(userProduct);
        String expectedJson = objectMapper.writeValueAsString(serviceResponse);

        // Act & Assert
        mockMvc.perform(post(BASE_URL + "/OneProductPayment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson, true));
    }

    @Test
    void settleAllPayment_ShouldReturnSettlementStatus() throws Exception {
        // Arrange
        UserProductsResponse<String> serviceResponse = new UserProductsResponse<>("All payments settled", 200, "Success");

        when(paymentService.settleAllPayment(anyString()))
                .thenReturn(new ResponseEntity<>(serviceResponse, HttpStatus.OK));

        String expectedJson = objectMapper.writeValueAsString(serviceResponse);

        // Act & Assert
        mockMvc.perform(post(BASE_URL + "/settleAllPayment/" + TEST_USER))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson, true));
    }
}