package com.pcProject.ecomOrderService.controller;

import com.pcProject.ecomOrderService.model.OrderDetails;
import com.pcProject.ecomOrderService.model.OrderDetailsWrapper;
import com.pcProject.ecomOrderService.model.UserProducts;
import com.pcProject.ecomOrderService.model.UserProductsResponse;
import com.pcProject.ecomOrderService.service.OrderDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("order")
public class OrderDetailsController {

    @Autowired
    private OrderDetailsService orderService;

    // Get all ordered Product of a user
    @GetMapping("getAllOrderDetails/{userName}")
    public ResponseEntity<UserProductsResponse<List<OrderDetails>>> getAllOrderDetails(@PathVariable String userName){
        return orderService.getAllOrderDetails(userName);
    }

    @PostMapping("getOneOrderDetails")
    public ResponseEntity<UserProductsResponse<OrderDetails>> getOneOrderDetails(@RequestBody UserProducts userProduct){
        return orderService.getOneOrderDetails(userProduct);
    }

    // Order a product for a user
    @PostMapping("addOrderDetails")
    public ResponseEntity<UserProductsResponse<OrderDetails>> addOrderDetails(@RequestBody UserProducts products){
        return orderService.addOrderDetails(products);
    }

    // Delete an ordered Product for a User
    @DeleteMapping("deleteOrderDetails")
    public ResponseEntity<UserProductsResponse<OrderDetailsWrapper>> deleteOrderDetails(@RequestBody OrderDetailsWrapper orderDetailsWrapper){
        return orderService.deleteOrderDetails(orderDetailsWrapper);
    }

    // Admin Action
    @PostMapping("updateOrderStatus/{orderId}")
    public ResponseEntity<UserProductsResponse<OrderDetails>> updateOrderStatus(@PathVariable int orderId){
        return orderService.updateOrderStatus(orderId);
    }
}
