package com.pcProject.ecomProducts.service;

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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.pcProject.ecomProducts.model.OrderDetailsWrapper;
import com.pcProject.ecomProducts.model.ProductDetails;
import com.pcProject.ecomProducts.model.ProductWrapper;
import com.pcProject.ecomProducts.model.UserProductsResponse;
import com.pcProject.ecomProducts.repository.ProductDetailsRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class ProductDetailsServiceTest {

    @Mock
    private ProductDetailsRepo productRepo;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ProductDetailsService productService;

    private static final String TEST_PRODUCT = "test-product";
    private ProductWrapper productWrapper;
    private ProductDetails productDetails;

    @BeforeEach
    void setUp() {
        productWrapper = new ProductWrapper();
        productWrapper.setProductName(TEST_PRODUCT);

        productDetails = new ProductDetails();
        productDetails.setProductName(TEST_PRODUCT);
        productDetails.setProductQuantity(10);
        productDetails.setProductValue(500);
    }

    // --- getProductDetails Branches ---

    @Test
    void getProductDetails_WhenProductExists_ShouldReturnOk() {
        when(productRepo.findByProductName(TEST_PRODUCT)).thenReturn(productDetails);
        ResponseEntity<?> response = (ResponseEntity<?>) productService.getProductDetails(productWrapper);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(productDetails, response.getBody());
    }

    @Test
    void getProductDetails_WhenProductDoesNotExist_ShouldReturnBadRequest() {
        when(productRepo.findByProductName(TEST_PRODUCT)).thenReturn(null);
        ResponseEntity<?> response = (ResponseEntity<?>) productService.getProductDetails(productWrapper);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Could not find the Product with given name", response.getBody());
    }

    // --- kafkaConsumer_preserveProduct Branches ---

    @Test
    void kafkaConsumer_preserveProduct_WhenValidJson_ShouldIncrementQuantity() throws JsonProcessingException {
        String json = "{\"productName\":\"test-product\"}";
        OrderDetailsWrapper wrapper = new OrderDetailsWrapper();
        wrapper.setProductName(TEST_PRODUCT);

        when(objectMapper.readValue(eq(json), eq(OrderDetailsWrapper.class))).thenReturn(wrapper);
        when(productRepo.findByProductName(TEST_PRODUCT)).thenReturn(productDetails);

        productService.kafkaConsumer_preserveProduct(json);

        assertEquals(11, productDetails.getProductQuantity());
        verify(productRepo, times(1)).save(productDetails);
    }

    // --- getProductValue Branches ---

    @Test
    void getProductValue_WhenProductExists_ShouldReturnPrice() {
        when(productRepo.findByProductName(TEST_PRODUCT)).thenReturn(productDetails);
        ResponseEntity<UserProductsResponse<String>> response = productService.getProductValue(productWrapper);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("500", response.getBody().getData());
    }

    @Test
    void getProductValue_WhenProductDoesNotExist_ShouldReturnBadRequestInBody() {
        when(productRepo.findByProductName(TEST_PRODUCT)).thenReturn(null);
        ResponseEntity<UserProductsResponse<String>> response = productService.getProductValue(productWrapper);
        assertEquals(HttpStatus.OK, response.getStatusCode()); // Implementation returns OK status code but BAD_REQUEST in body
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
    }

    // --- Admin Actions ---

    @Test
    void getAllProducts_ShouldReturnList() {
        when(productRepo.findAll()).thenReturn(Collections.singletonList(productDetails));
        ResponseEntity<?> response = (ResponseEntity<?>) productService.getAllProducts();
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void addProductDetails_ShouldReturnCreated() {
        ResponseEntity<?> response = (ResponseEntity<?>) productService.addProductDetails(productDetails);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(productRepo, times(1)).save(productDetails);
    }

    // --- addToProductQuantity Branches ---

    @Test
    void addToProductQuantity_WhenProductExists_ShouldIncreaseQuantity() {
        Map<String, String> map = new HashMap<>();
        map.put("productName", TEST_PRODUCT);
        map.put("productQuantity", "5");

        when(productRepo.findByProductName(TEST_PRODUCT)).thenReturn(productDetails);

        ResponseEntity<?> response = (ResponseEntity<?>) productService.addToProductQuantity(map);

        assertEquals(15, productDetails.getProductQuantity());
        verify(productRepo, times(1)).save(productDetails);
    }

    @Test
    void addToProductQuantity_WhenProductDoesNotExist_ShouldReturnError() {
        Map<String, String> map = new HashMap<>();
        map.put("productName", "unknown");

        when(productRepo.findByProductName("unknown")).thenReturn(null);

        ResponseEntity<?> response = (ResponseEntity<?>) productService.addToProductQuantity(map);
        UserProductsResponse<?> body = (UserProductsResponse<?>) response.getBody();
        assertEquals(HttpStatus.BAD_REQUEST.value(), body.getStatus());
        assertNull(body.getData());
    }

    // --- updateProductDetails Branches ---

    @Test
    void updateProductDetails_WhenProductExists_ShouldUpdateAndReturnOk() {
        ProductDetails updateReq = new ProductDetails();
        updateReq.setProductName(TEST_PRODUCT);
        updateReq.setProductValue(999);
        updateReq.setProductQuantity(20);

        when(productRepo.findByProductName(TEST_PRODUCT)).thenReturn(productDetails);

        ResponseEntity<?> response = (ResponseEntity<?>) productService.updateProductDetails(updateReq);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(999, productDetails.getProductValue());
        verify(productRepo, times(1)).save(productDetails);
    }

    @Test
    void updateProductDetails_WhenProductDoesNotExist_ShouldReturnBadRequest() {
        when(productRepo.findByProductName(anyString())).thenReturn(null);
        ResponseEntity<?> response = (ResponseEntity<?>) productService.updateProductDetails(productDetails);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // --- deleteProductDetails Branches ---

    @Test
    void deleteProductDetails_WhenProductExists_ShouldDelete() {
        when(productRepo.findByProductName(TEST_PRODUCT)).thenReturn(productDetails);
        ResponseEntity<?> response = (ResponseEntity<?>) productService.deleteProductDetails(TEST_PRODUCT);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productRepo, times(1)).deleteByProductName(TEST_PRODUCT);
    }

    @Test
    void deleteProductDetails_WhenProductDoesNotExist_ShouldFail() {
        when(productRepo.findByProductName(TEST_PRODUCT)).thenReturn(null);
        ResponseEntity<?> response = (ResponseEntity<?>) productService.deleteProductDetails(TEST_PRODUCT);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(productRepo, never()).deleteByProductName(anyString());
    }

    // --- validateProduct Branches ---

    @Test
    void validateProduct_WhenProductExists_ShouldReturnSuccess() {
        when(productRepo.findByProductName(TEST_PRODUCT)).thenReturn(productDetails);
        ResponseEntity<UserProductsResponse<ProductWrapper>> response = productService.validateProduct(productWrapper);
        assertEquals(HttpStatus.OK.value(), response.getBody().getStatus());
        assertEquals("Validation Successful", response.getBody().getMessage());
    }

    @Test
    void validateProduct_WhenProductDoesNotExist_ShouldReturnInvalid() {
        when(productRepo.findByProductName(TEST_PRODUCT)).thenReturn(null);
        ResponseEntity<UserProductsResponse<ProductWrapper>> response = productService.validateProduct(productWrapper);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
    }

    // --- consumeProduct Branches ---

    @Test
    void consumeProduct_WhenProductExistsAndHasStock_ShouldDecrement() {
        when(productRepo.findByProductName(TEST_PRODUCT)).thenReturn(productDetails);
        ResponseEntity<UserProductsResponse<ProductWrapper>> response = productService.consumeProduct(productWrapper);

        assertEquals(9, productDetails.getProductQuantity());
        assertEquals(HttpStatus.OK.value(), response.getBody().getStatus());
        verify(productRepo, times(1)).save(productDetails);
    }

    @Test
    void consumeProduct_WhenProductDoesNotExist_ShouldReturnInvalid() {
        when(productRepo.findByProductName(TEST_PRODUCT)).thenReturn(null);
        ResponseEntity<UserProductsResponse<ProductWrapper>> response = productService.consumeProduct(productWrapper);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
        assertEquals("Invalid Product", response.getBody().getMessage());
    }

    @Test
    void consumeProduct_WhenStockIsZero_ShouldReturnNotAvailable() {
        productDetails.setProductQuantity(0);
        when(productRepo.findByProductName(TEST_PRODUCT)).thenReturn(productDetails);

        ResponseEntity<UserProductsResponse<ProductWrapper>> response = productService.consumeProduct(productWrapper);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
        assertEquals("Product is not available at the movement", response.getBody().getMessage());
        verify(productRepo, never()).save(any());
    }
}