package com.pcProject.ecomProducts.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProductsResponse<T> {
    private T data ;
    private int status;
    private String message;
}