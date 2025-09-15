package com.pcProject.ecomOrderService.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "order_details")
public class OrderDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int orderId;
    private String userName;
    private String productName;
    private String orderStatus;
    private String paymentStatus = "Unpaid";

    public OrderDetails(int orderId, String userName, String productName, String orderStatus) {
        this.orderId = orderId;
        this.userName = userName;
        this.productName = productName;
        this.orderStatus = orderStatus;
    }
}