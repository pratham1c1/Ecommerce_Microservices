package com.pcProject.ecomOrderService.controller;

import com.pcProject.ecomOrderService.model.UserProducts;
import com.pcProject.ecomOrderService.model.UserProductsResponse;
import com.pcProject.ecomOrderService.service.PaymentDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("payment")
public class PaymentDetailsController {

    @Autowired
    private PaymentDetailsService paymentService;

    @GetMapping("totalPayment/{userName}")
    public ResponseEntity<UserProductsResponse<String>> getAllPayment(@PathVariable String userName){
        return paymentService.getAllPayment(userName);
    }

    @PostMapping("OneProductPayment")
    public ResponseEntity<UserProductsResponse<UserProducts>> OneProductPayment(@RequestBody UserProducts userProduct){
        return paymentService.OneProductPayment(userProduct);
    }

    @PostMapping("settleAllPayment/{userName}")
    public ResponseEntity<UserProductsResponse<String>> settleAllPayment(@PathVariable String userName){
        return paymentService.settleAllPayment(userName);
    }
}
