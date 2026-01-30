package com.pcProject.ecomUsers.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailsWrapper {
    private int orderId;
    private String userName;
    private String productName;
}
