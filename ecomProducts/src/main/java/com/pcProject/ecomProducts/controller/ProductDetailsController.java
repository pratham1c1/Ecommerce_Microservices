package com.pcProject.ecomProducts.controller;

import com.pcProject.ecomProducts.model.ProductDetails;
import com.pcProject.ecomProducts.model.ProductWrapper;
import com.pcProject.ecomProducts.model.UserProductsResponse;
import com.pcProject.ecomProducts.service.ProductDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("product")
public class ProductDetailsController {

    @Autowired
    private ProductDetailsService productService;

    //    -----------------------------------------
    // Exposed to OrderService
    @PostMapping("consumeProduct")
    public ResponseEntity<UserProductsResponse<ProductWrapper>> consumeProduct(@RequestBody ProductWrapper product){
        return productService.consumeProduct(product);
    }

    @PostMapping("getProductValue")
    public ResponseEntity<UserProductsResponse<String>> getProductValue(@RequestBody ProductWrapper product){
        return productService.getProductValue(product);
    }

    @PostMapping("validateProduct")
    public ResponseEntity<UserProductsResponse<ProductWrapper>> validateProduct(@RequestBody ProductWrapper product){
        return productService.validateProduct(product);
    }

    //    -----------------------------------------

    // Admin Actions
    @PostMapping("getProductDetails")
    public Object getProductDetails(@RequestBody ProductWrapper product){
        return productService.getProductDetails(product);
    }

    @GetMapping("getAllProducts")
    public Object getAllProducts(){
        return productService.getAllProducts();
    }

    @PostMapping("addProductDetails")
    public Object addProductDetails(@RequestBody ProductDetails product){
        return productService.addProductDetails(product);
    }

    @PostMapping("addToProductQuantity")
    public Object addToProductQuantity(@RequestBody Map<String,String> productDetails){
        return productService.addToProductQuantity(productDetails);
    }

    @PutMapping("updateProductDetails")
    public Object updateProductDetails(@RequestBody ProductDetails product){
        return productService.updateProductDetails(product);
    }

    @DeleteMapping("deleteProductDetails/{productName}")
    public Object deleteProductDetails(@PathVariable String productName){
        return productService.deleteProductDetails(productName);
    }

}