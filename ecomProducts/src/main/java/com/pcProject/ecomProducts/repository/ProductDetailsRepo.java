package com.pcProject.ecomProducts.repository;

import com.pcProject.ecomProducts.model.ProductDetails;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductDetailsRepo extends JpaRepository<ProductDetails,Integer> {
    ProductDetails findByProductName(String productName);

    @Modifying
    @Transactional
    @Query("Delete from ProductDetails where productName = :productName")
    void deleteByProductName(String productName);
}
