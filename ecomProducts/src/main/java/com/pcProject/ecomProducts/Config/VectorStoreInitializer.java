package com.pcProject.ecomProducts.Config;

import com.pcProject.ecomProducts.service.ProductDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VectorStoreInitializer implements ApplicationRunner {
    private final ProductDetailsService productDetailsService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        productDetailsService.loadFromDatabase();
    }
}
