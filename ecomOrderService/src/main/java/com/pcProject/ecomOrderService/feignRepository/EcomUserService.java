package com.pcProject.ecomOrderService.feignRepository;

import com.pcProject.ecomOrderService.model.UserProducts;
import com.pcProject.ecomOrderService.model.UserProductsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.CacheRequest;
import java.util.List;

@FeignClient("ECOMUSERS")
public interface EcomUserService {
    @GetMapping("/users/getAllUserProducts/{userName}")
    public ResponseEntity<UserProductsResponse<List<String>>> getAllUserProducts(@PathVariable String userName);

    @PostMapping("/users/addUserProduct")
    public ResponseEntity<UserProductsResponse<UserProducts>> addUserProduct(@RequestBody UserProducts userProduct);


    @PostMapping("/users/removeUserProduct")
    public ResponseEntity<UserProductsResponse<UserProducts>> removeUserProduct(@RequestBody UserProducts userProduct);

    @PostMapping("/users/validateUserProduct")
    public ResponseEntity<UserProductsResponse<UserProducts>> validateUserProduct(@RequestBody UserProducts userProducts);
}
