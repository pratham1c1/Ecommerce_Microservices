package com.pcProject.ecomProducts.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Entity
@Table(name = "product_details")
@AllArgsConstructor
@NoArgsConstructor
public class ProductDetails {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int productId;
    @Column(unique = true)
    private String productName;
    private int productValue;
    private int productQuantity;
}