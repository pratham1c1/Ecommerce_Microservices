package com.pcProject.ecomProducts.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import com.pcProject.ecomProducts.model.ProductDetails;
import com.pcProject.ecomProducts.model.ProductWrapper;
import com.pcProject.ecomProducts.model.UserProductsResponse;
import com.pcProject.ecomProducts.service.ProductDetailsService;
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

@WebMvcTest(ProductDetailsController.class)
class ProductDetailsControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductDetailsService productService;

    private static final String TEST_USER = "test-userName";
    private static final String TEST_PRODUCT = "test-productName";
    private static final String BASE_URL = "/product/";

    private ProductWrapper productWrapper;
    private ProductDetails productDetails;

    @BeforeEach
    void setUp() {
        productWrapper = new ProductWrapper();
        productWrapper.setProductName(TEST_PRODUCT);

        productDetails = new ProductDetails();
        productDetails.setProductName(TEST_PRODUCT);
    }

    @Test
    void consumeProduct_WhenValidRequest_ShouldReturnOk() throws Exception {
        UserProductsResponse<ProductWrapper> response = new UserProductsResponse<>(productWrapper, 200, "Consumed");
        when(productService.consumeProduct(any())).thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        mockMvc.perform(post(BASE_URL + "consumeProduct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productWrapper)))
                .andExpect(status().isOk());
    }

    @Test
    void getProductValue_WhenValidRequest_ShouldReturnOk() throws Exception {
        UserProductsResponse<String> response = new UserProductsResponse<>("100", 200, "Success");
        when(productService.getProductValue(any())).thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        mockMvc.perform(post(BASE_URL + "getProductValue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productWrapper)))
                .andExpect(status().isOk());
    }

    @Test
    void validateProduct_WhenValidRequest_ShouldReturnOk() throws Exception {
        UserProductsResponse<ProductWrapper> response = new UserProductsResponse<>(productWrapper, 200, "Valid");
        when(productService.validateProduct(any())).thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        mockMvc.perform(post(BASE_URL + "validateProduct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productWrapper)))
                .andExpect(status().isOk());
    }

    @Test
    void getProductDetails_WhenValidRequest_ShouldReturnObject() throws Exception {
        when(productService.getProductDetails(any())).thenReturn(productDetails);

        mockMvc.perform(post(BASE_URL + "getProductDetails")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productWrapper)))
                .andExpect(status().isOk());
    }

    @Test
    void getAllProducts_WhenCalled_ShouldReturnList() throws Exception {
        when(productService.getAllProducts()).thenReturn("List of Products");

        mockMvc.perform(get(BASE_URL + "getAllProducts"))
                .andExpect(status().isOk());
    }

    @Test
    void addProductDetails_WhenValidRequest_ShouldReturnSuccess() throws Exception {
        when(productService.addProductDetails(any())).thenReturn("Added Successfully");

        mockMvc.perform(post(BASE_URL + "addProductDetails")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDetails)))
                .andExpect(status().isOk());
    }

    @Test
    void addToProductQuantity_WhenValidRequest_ShouldReturnSuccess() throws Exception {
        Map<String, String> details = new HashMap<>();
        details.put("productName", TEST_PRODUCT);
        details.put("quantity", "10");

        when(productService.addToProductQuantity(any())).thenReturn("Quantity Updated");

        mockMvc.perform(post(BASE_URL + "addToProductQuantity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(details)))
                .andExpect(status().isOk());
    }

    @Test
    void updateProductDetails_WhenValidRequest_ShouldReturnSuccess() throws Exception {
        when(productService.updateProductDetails(any())).thenReturn("Updated Successfully");

        mockMvc.perform(put(BASE_URL + "updateProductDetails")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDetails)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteProductDetails_WhenValidProductName_ShouldReturnSuccess() throws Exception {
        when(productService.deleteProductDetails(anyString())).thenReturn("Deleted Successfully");

        mockMvc.perform(delete(BASE_URL + "deleteProductDetails/" + TEST_PRODUCT))
                .andExpect(status().isOk());
    }
}