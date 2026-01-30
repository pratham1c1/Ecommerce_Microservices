package com.pcProject.ecomOrderService.feignRepository;

import com.pcProject.ecomOrderService.model.ProductWrapper;
import com.pcProject.ecomOrderService.model.UserProductsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("ECOMPRODUCTS")
public interface EcomProductService {

    @PostMapping ("/product/consumeProduct")
    public ResponseEntity<UserProductsResponse<ProductWrapper>> consumeProduct(@RequestBody ProductWrapper product);

    @PostMapping("/product/getProductValue")
    public ResponseEntity<UserProductsResponse<String>> getProductValue(@RequestBody ProductWrapper product);
}