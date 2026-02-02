package com.pcProject.ecomOrderService.service;

import com.pcProject.ecomOrderService.feignRepository.EcomProductService;
import com.pcProject.ecomOrderService.feignRepository.EcomUserService;
import com.pcProject.ecomOrderService.model.OrderDetails;
import com.pcProject.ecomOrderService.model.ProductWrapper;
import com.pcProject.ecomOrderService.model.UserProducts;
import com.pcProject.ecomOrderService.model.UserProductsResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentDetailsServiceTest {
    @Mock
    private EcomUserService ecomUserService;
    @Mock
    private EcomProductService ecomProductService;
    @Mock
    private OrderDetailsService orderDetailsService;
    @InjectMocks
    private PaymentDetailsService paymentDetailsService;

    private UserProducts userProduct;
    private UserProductsResponse<List<String>> successUserProductResponse;
    private static final String TEST_USER = "test-TEST_USER";
    private static final String TEST_PRODUCT = "test-productName";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userProduct = new UserProducts(TEST_USER, TEST_PRODUCT);
        successUserProductResponse = new UserProductsResponse<>(Arrays.asList(TEST_PRODUCT), 200, "Success");

        // Setup shared initialization if necessary
    }

    // --- getAllPayment Branch Coverage ---

    @Test
    void getAllPayment_WhenServiceResponseIsNull_ShouldReturnBadRequest() {
        when(ecomUserService.getAllUserProducts(TEST_USER)).thenReturn(ResponseEntity.ok(null));
        ResponseEntity<UserProductsResponse<String>> result = paymentDetailsService.getAllPayment(TEST_USER);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals(500, result.getBody().getStatus());
    }

    @Test
    void getAllPayment_WhenStatusNot200_ShouldReturnBadRequest() {
        UserProductsResponse<List<String>> failResponse = new UserProductsResponse<>(null, 404, "Not Found");
        when(ecomUserService.getAllUserProducts(TEST_USER)).thenReturn(ResponseEntity.ok(failResponse));
        ResponseEntity<UserProductsResponse<String>> result = paymentDetailsService.getAllPayment(TEST_USER);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    void getAllPayment_WhenProductValueResponseIsNull_ShouldNotIncrementTotal() {
        // Case 1: productValueResponse is null
        when(ecomUserService.getAllUserProducts(TEST_USER)).thenReturn(ResponseEntity.ok(successUserProductResponse));
        when(ecomProductService.getProductValue(any())).thenReturn(null);

        ResponseEntity<UserProductsResponse<String>> result = paymentDetailsService.getAllPayment(TEST_USER);
        assertEquals("0", result.getBody().getData());
    }

    @Test
    void getAllPayment_WhenProductValueBodyIsNull_ShouldNotIncrementTotal() {
        // Case 2: productValueResponse is not null + getBody is null
        when(ecomUserService.getAllUserProducts(TEST_USER)).thenReturn(ResponseEntity.ok(successUserProductResponse));
        when(ecomProductService.getProductValue(any())).thenReturn(ResponseEntity.ok(null));

        ResponseEntity<UserProductsResponse<String>> result = paymentDetailsService.getAllPayment(TEST_USER);
        assertEquals("0", result.getBody().getData());
    }

    @Test
    void getAllPayment_WhenProductValueIsValid_ShouldReturnTotal() {
        // Case 3: productValueResponse is not null + getBody is not null
        when(ecomUserService.getAllUserProducts(TEST_USER)).thenReturn(ResponseEntity.ok(successUserProductResponse));
        UserProductsResponse<String> val = new UserProductsResponse<>("100", 200, "OK");
        when(ecomProductService.getProductValue(any())).thenReturn(ResponseEntity.ok(val));

        ResponseEntity<UserProductsResponse<String>> result = paymentDetailsService.getAllPayment(TEST_USER);
        assertEquals("100", result.getBody().getData());
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    // --- OneProductPayment Branch Coverage ---

    @Test
    void OneProductPayment_WhenUserResponseIsNull_ShouldReturnBadRequest() {
        when(ecomUserService.getAllUserProducts(TEST_USER)).thenReturn(ResponseEntity.ok(null));
        ResponseEntity<UserProductsResponse<UserProducts>> result = paymentDetailsService.OneProductPayment(userProduct);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    void OneProductPayment_WhenUserStatusNot200_ShouldReturnBadRequest() {
        UserProductsResponse<List<String>> failResponse = new UserProductsResponse<>(null, 401, "Unauthorized");
        when(ecomUserService.getAllUserProducts(TEST_USER)).thenReturn(ResponseEntity.ok(failResponse));
        ResponseEntity<UserProductsResponse<UserProducts>> result = paymentDetailsService.OneProductPayment(userProduct);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    void OneProductPayment_WhenProductNotInOrderList_ShouldReturnBadRequest() {
        UserProductsResponse<List<String>> response = new UserProductsResponse<>(Arrays.asList("OtherProduct"), 200, "OK");
        when(ecomUserService.getAllUserProducts(TEST_USER)).thenReturn(ResponseEntity.ok(response));
        ResponseEntity<UserProductsResponse<UserProducts>> result = paymentDetailsService.OneProductPayment(userProduct);
        assertEquals("Product is not available in Order List", result.getBody().getMessage());
    }

    @Test
    void OneProductPayment_WhenOrderDetailsStatusNot200_ShouldReturnBadRequest() {
        when(ecomUserService.getAllUserProducts(TEST_USER)).thenReturn(ResponseEntity.ok(successUserProductResponse));
        UserProductsResponse<OrderDetails> orderFail = new UserProductsResponse<>(null, 500, "DB Error");
        when(orderDetailsService.getOneOrderDetails(any())).thenReturn(ResponseEntity.ok(orderFail));

        ResponseEntity<UserProductsResponse<UserProducts>> result = paymentDetailsService.OneProductPayment(userProduct);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    void OneProductPayment_WhenUpdatePaymentStatusFails_ShouldReturnBadRequest() {
        when(ecomUserService.getAllUserProducts(TEST_USER)).thenReturn(ResponseEntity.ok(successUserProductResponse));
        UserProductsResponse<OrderDetails> orderSuccess = new UserProductsResponse<>(new OrderDetails(), 200, "OK");
        when(orderDetailsService.getOneOrderDetails(any())).thenReturn(ResponseEntity.ok(orderSuccess));
        when(orderDetailsService.updatePaymentStatus(any())).thenReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));

        ResponseEntity<UserProductsResponse<UserProducts>> result = paymentDetailsService.OneProductPayment(userProduct);
        assertEquals("Something went wrong while updating status!", result.getBody().getMessage());
    }

    @Test
    void OneProductPayment_WhenUpdateOrderStatusFails_ShouldReturnBadRequest() {
        when(ecomUserService.getAllUserProducts(TEST_USER)).thenReturn(ResponseEntity.ok(successUserProductResponse));
        OrderDetails details = new OrderDetails();
        details.setOrderId(101);
        UserProductsResponse<OrderDetails> orderSuccess = new UserProductsResponse<>(details, 200, "OK");
        when(orderDetailsService.getOneOrderDetails(any())).thenReturn(ResponseEntity.ok(orderSuccess));
        when(orderDetailsService.updatePaymentStatus(any())).thenReturn(ResponseEntity.ok(null));
        when(orderDetailsService.updateOrderStatus(101)).thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        ResponseEntity<UserProductsResponse<UserProducts>> result = paymentDetailsService.OneProductPayment(userProduct);
        assertEquals("Payment is due for the User Product.", result.getBody().getMessage());
    }

    @Test
    void OneProductPayment_WhenSuccessful_ShouldReturnOk() {
        when(ecomUserService.getAllUserProducts(TEST_USER)).thenReturn(ResponseEntity.ok(successUserProductResponse));
        OrderDetails details = new OrderDetails();
        details.setOrderId(101);
        UserProductsResponse<OrderDetails> orderSuccess = new UserProductsResponse<>(details, 200, "OK");
        when(orderDetailsService.getOneOrderDetails(any())).thenReturn(ResponseEntity.ok(orderSuccess));
        when(orderDetailsService.updatePaymentStatus(any())).thenReturn(ResponseEntity.ok(null));
        when(orderDetailsService.updateOrderStatus(101)).thenReturn(ResponseEntity.ok(null));

        ResponseEntity<UserProductsResponse<UserProducts>> result = paymentDetailsService.OneProductPayment(userProduct);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Payment Successful", result.getBody().getMessage());
    }

    // --- settleAllPayment Branch Coverage ---

    @Test
    void settleAllPayment_WhenUserResponseIsNull_ShouldReturnBadRequest() {
        when(ecomUserService.getAllUserProducts(TEST_USER)).thenReturn(ResponseEntity.ok(null));
        ResponseEntity<UserProductsResponse<String>> result = paymentDetailsService.settleAllPayment(TEST_USER);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    void settleAllPayment_WhenUserStatusNot200_ShouldReturnBadRequest() {
        UserProductsResponse<List<String>> failResponse = new UserProductsResponse<>(null, 500, "Error");
        when(ecomUserService.getAllUserProducts(TEST_USER)).thenReturn(ResponseEntity.ok(failResponse));
        ResponseEntity<UserProductsResponse<String>> result = paymentDetailsService.settleAllPayment(TEST_USER);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    void settleAllPayment_WhenSuccessful_ShouldReturnOkAndRemoveProducts() {
        when(ecomUserService.getAllUserProducts(TEST_USER)).thenReturn(ResponseEntity.ok(successUserProductResponse));
        UserProductsResponse<String> val = new UserProductsResponse<>("250", 200, "OK");
        when(ecomProductService.getProductValue(any())).thenReturn(ResponseEntity.ok(val));

        ResponseEntity<UserProductsResponse<String>> result = paymentDetailsService.settleAllPayment(TEST_USER);

        assertEquals("250", result.getBody().getData());
        assertEquals("Payment settled", result.getBody().getMessage());
        verify(ecomUserService).removeUserProduct(any());
    }
    @Test
    void settleAllPayment_WhenProductValueResponseIsNull_ShouldReturnBadRequest() {
        when(ecomUserService.getAllUserProducts(TEST_USER)).thenReturn(ResponseEntity.ok(successUserProductResponse));
        UserProductsResponse<String> val = new UserProductsResponse<>("250", 200, "OK");
        when(ecomProductService.getProductValue(any())).thenReturn(null);

        ResponseEntity<UserProductsResponse<String>> result = paymentDetailsService.settleAllPayment(TEST_USER);


        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("Something went wrong !", result.getBody().getMessage());
        verify(ecomUserService, times(1)).removeUserProduct(any());
    }
    @Test
    void settleAllPayment_WhenProductValueResponseBodyIsNull_ShouldReturnBadRequest() {
        when(ecomUserService.getAllUserProducts(TEST_USER)).thenReturn(ResponseEntity.ok(successUserProductResponse));
        UserProductsResponse<String> val = new UserProductsResponse<>("250", 200, "OK");
        when(ecomProductService.getProductValue(any())).thenReturn(ResponseEntity.ok(null));

        ResponseEntity<UserProductsResponse<String>> result = paymentDetailsService.settleAllPayment(TEST_USER);


        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("Something went wrong !", result.getBody().getMessage());
        verify(ecomUserService, times(1)).removeUserProduct(any());
    }
}