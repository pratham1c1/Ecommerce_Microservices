package com.pcProject.ecomOrderService.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailsWrapper {
    private int orderId;
    private String userName;
    private String productName;
}
