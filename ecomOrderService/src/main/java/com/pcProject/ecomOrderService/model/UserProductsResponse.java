package com.pcProject.ecomOrderService.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProductsResponse<T> {
    private T data;
    private int status;
    private String message;
}