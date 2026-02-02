package com.pcProject.ecomOrderService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pcProject.ecomOrderService.model.OrderDetails;
import com.pcProject.ecomOrderService.model.OrderDetailsWrapper;
import com.pcProject.ecomOrderService.model.UserProducts;
import com.pcProject.ecomOrderService.model.UserProductsResponse;
import com.pcProject.ecomOrderService.service.OrderDetailsService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderDetailsController.class)
class OrderDetailsControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderDetailsService orderService;

    private static OrderDetails orderDetails;
    private static UserProducts testProduct;
    private static final String TEST_USER = "test-userName";
    private static final String TEST_PRODUCT = "test-productName";
    private static final String BASE_URL = "/order/";


    @BeforeAll
    static void setUp(){
        testProduct = new UserProducts(TEST_USER, TEST_PRODUCT);
        orderDetails = new OrderDetails(101,TEST_USER,TEST_PRODUCT,"Unpaid");
    }

    @Test
    void getAllOrderDetails_WhenUserNameIsValid_ShouldReturn200() throws Exception {
        UserProductsResponse<List<OrderDetails>> testUserProductResponse = new UserProductsResponse<>(List.of(orderDetails),200,"Successfully Fetched the details");
        String expectedJsonResponse = objectMapper.writeValueAsString(testUserProductResponse);

        when(orderService.getAllOrderDetails(TEST_USER)).thenReturn(ResponseEntity.ok(testUserProductResponse));

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL+"getAllOrderDetails/{userName}",TEST_USER))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJsonResponse,true));
    }

    @Test
    void getAllOrderDetails_WhenServiceReturnsErrorBody_ShouldReturn400() throws Exception {

        UserProductsResponse<List<OrderDetails>> testUserProductResponse = new UserProductsResponse<>(new ArrayList<>(),500,"Something went wrong !");
        String expectedJsonResponse = objectMapper.writeValueAsString(testUserProductResponse);

        when(orderService.getAllOrderDetails(TEST_USER)).thenReturn(ResponseEntity.badRequest().body(testUserProductResponse));

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL+"getAllOrderDetails/{userName}",TEST_USER))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(expectedJsonResponse));
    }

    @Test
    void getAllOrderDetails_WhenUserNameIsNotPresentInDB_ShouldReturn400() throws Exception {
        UserProductsResponse<List<OrderDetails>> testUserProductResponse = new UserProductsResponse<>(new ArrayList<>(),400,"Could not find the User with given name");
        String expectedJsonResponse = objectMapper.writeValueAsString(testUserProductResponse);

        when(orderService.getAllOrderDetails(TEST_USER)).thenReturn(ResponseEntity.badRequest().body(testUserProductResponse));

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL+"getAllOrderDetails/{userName}",TEST_USER))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(expectedJsonResponse));

    }

    @Test
    void getOneOrderDetails_ShouldReturnOrder() throws Exception {
//        OrderDetails order = new OrderDetails(101, TEST_USER, TEST_PRODUCT, "Placed", "Unpaid");
        UserProductsResponse<OrderDetails> serviceResponse = new UserProductsResponse<>(orderDetails, 200, "Success");

        when(orderService.getOneOrderDetails(any(UserProducts.class)))
                .thenReturn(new ResponseEntity<>(serviceResponse, HttpStatus.OK));

        String requestJson = objectMapper.writeValueAsString(testProduct);
        String expectedJson = objectMapper.writeValueAsString(serviceResponse);

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL+"/getOneOrderDetails")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson, true));
    }

    @Test
    void addOrderDetails_ShouldReturnCreatedOrder() throws Exception {
        OrderDetails newOrder = new OrderDetails(102, TEST_USER, TEST_PRODUCT, "Waiting_to_Place", "UnPaid");
        UserProductsResponse<OrderDetails> serviceResponse = new UserProductsResponse<>(newOrder, 200, "Order received");

        when(orderService.addOrderDetails(any(UserProducts.class)))
                .thenReturn(new ResponseEntity<>(serviceResponse, HttpStatus.OK));

        String requestJson = objectMapper.writeValueAsString(testProduct);
        String expectedJson = objectMapper.writeValueAsString(serviceResponse);

        mockMvc.perform(post(BASE_URL+"/addOrderDetails")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson, true));
    }

    @Test
    void deleteOrderDetails_ShouldReturnOkStatus() throws Exception {
        OrderDetailsWrapper wrapper = new OrderDetailsWrapper(101, TEST_USER, TEST_PRODUCT);
        UserProductsResponse<OrderDetailsWrapper> serviceResponse = new UserProductsResponse<>(wrapper, 200, "Deleted Successfully");

        when(orderService.deleteOrderDetails(any(OrderDetailsWrapper.class)))
                .thenReturn(new ResponseEntity<>(serviceResponse, HttpStatus.OK));

        String requestJson = objectMapper.writeValueAsString(wrapper);
        String expectedJson = objectMapper.writeValueAsString(serviceResponse);

        mockMvc.perform(delete(BASE_URL+"/deleteOrderDetails")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson, true));
    }

    @Test
    void updateOrderStatus_ShouldUpdateViaPathVariable() throws Exception {
        OrderDetails updatedOrder = new OrderDetails(101, TEST_USER, TEST_PRODUCT, "Shipped", "Paid");
        UserProductsResponse<OrderDetails> serviceResponse = new UserProductsResponse<>(updatedOrder, 200, "Successfully updated the order Status");

        when(orderService.updateOrderStatus(anyInt()))
                .thenReturn(new ResponseEntity<>(serviceResponse, HttpStatus.OK));

        String expectedJson = objectMapper.writeValueAsString(serviceResponse);

        mockMvc.perform(post(BASE_URL+"/updateOrderStatus/101")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson, true));
    }
}