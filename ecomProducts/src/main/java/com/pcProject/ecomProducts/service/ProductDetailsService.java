package com.pcProject.ecomProducts.service;

import com.pcProject.ecomProducts.model.ProductDetails;
import com.pcProject.ecomProducts.model.ProductWrapper;
import com.pcProject.ecomProducts.model.UserProductsResponse;
import com.pcProject.ecomProducts.repository.ProductDetailsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ProductDetailsService {
    @Autowired
    private ProductDetailsRepo productRepo;

    // To get the Product Details
    public Object getProductDetails(ProductWrapper product){
        ProductDetails existingProduct = productRepo.findByProductName(product.getProductName());
        if(existingProduct == null)
            return new ResponseEntity<>("Could not find the Product with given name",HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(existingProduct, HttpStatus.OK);
    }

    // To get the Product value using ProductWrapper
    public ResponseEntity<UserProductsResponse<String>> getProductValue(ProductWrapper product){
        ProductDetails existingProduct = productRepo.findByProductName(product.getProductName());
        UserProductsResponse<String> response = new UserProductsResponse<>();

        if(existingProduct == null){
            response.setData(null);
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMessage("Product doesn't exists");
            return new ResponseEntity<>(response,HttpStatus.OK);
        }
        response.setData(String.valueOf(existingProduct.getProductValue()));
        response.setStatus(HttpStatus.OK.value());
        response.setMessage("Returning the Product value");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    // Admin Actions
    public Object getAllProducts() {
        return new ResponseEntity<>(productRepo.findAll(),HttpStatus.OK);
    }


    // To add a Product
    public Object addProductDetails(ProductDetails product){
        productRepo.save(product);
        return new ResponseEntity<>(product,HttpStatus.CREATED);
    }

    // To add into Product quantity
    public Object addToProductQuantity(Map<String,String> productDetails){
        ProductDetails existingProduct = productRepo.findByProductName(productDetails.get("productName"));
        UserProductsResponse<ProductDetails> response = new UserProductsResponse<>();

        if(existingProduct == null){
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMessage("Invalid Product");
        }
        else{
            existingProduct.setProductQuantity(existingProduct.getProductQuantity()+Integer.parseInt(productDetails.get("productQuantity")));
            productRepo.save(existingProduct);
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Added to existing Product quantity");
        }

        response.setData(existingProduct);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    //To update an existing Product based on Product name
    public Object updateProductDetails(ProductDetails product){
        ProductDetails existingProduct = productRepo.findByProductName(product.getProductName());
        if(existingProduct == null)
            return new ResponseEntity<>("Could not find the Product with given name",HttpStatus.BAD_REQUEST);

        existingProduct.setProductValue(product.getProductValue());
        existingProduct.setProductQuantity(product.getProductQuantity());
        productRepo.save(existingProduct);
        return new ResponseEntity<>(existingProduct,HttpStatus.OK);
    }

    //To delete an existing Product based on Product name
    public Object deleteProductDetails(String productName){
        ProductDetails existingProduct = productRepo.findByProductName(productName);
        if(existingProduct == null)
            return new ResponseEntity<>("Could not find the Product with given name",HttpStatus.BAD_REQUEST);

        productRepo.deleteByProductName(productName);
        return new ResponseEntity<>("Deleted the Product Details successfully", HttpStatus.OK);
    }

    //    -----------------------------------------
    // Exposed to OrderService
    public ResponseEntity<UserProductsResponse<ProductWrapper>> validateProduct(ProductWrapper product) {
        ProductDetails existingProduct = productRepo.findByProductName(product.getProductName());
        UserProductsResponse<ProductWrapper> response = new UserProductsResponse<>();
        response.setData(product);

        if(existingProduct == null){
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMessage("Invalid Product");
        }
        else{
            response.setStatus(HttpStatus.OK.value());
            response.setMessage("Validation Successful");
        }

        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    public ResponseEntity<UserProductsResponse<ProductWrapper>> consumeProduct(ProductWrapper product) {
        ProductDetails existingProduct = productRepo.findByProductName(product.getProductName());
        UserProductsResponse<ProductWrapper> response = new UserProductsResponse<>();
        response.setData(product);

        if(existingProduct == null ){
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMessage("Invalid Product");
            return new ResponseEntity<>(response,HttpStatus.OK);
        }else if(existingProduct.getProductQuantity() == 0){
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMessage("Product is not available at the movement");
            return new ResponseEntity<>(response,HttpStatus.OK);
        }

        assert existingProduct != null;
        existingProduct.setProductQuantity(existingProduct.getProductQuantity()-1);
        productRepo.save(existingProduct);
        response.setStatus(HttpStatus.OK.value());
        response.setMessage("Returning the Product ");
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    //    -----------------------------------------


}
