package com.pcProject.ecomOrderService.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OrderDetailsTest {

    @Test
    void orderDetailsConstructor_ShouldReturnAnObjectWithUnpaidPaymentStatus(){
        int orderId = 101;
        String userName = "test-userName";
        String productName = "test-productName";
        String paymentStatus = "Paid";

        OrderDetails testOrderDetailsObj = new OrderDetails(orderId,userName,productName,paymentStatus);
        assertEquals("Unpaid",testOrderDetailsObj.getPaymentStatus());
    }

}