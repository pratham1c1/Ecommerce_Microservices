package com.pcProject.ecomOrderService.Repository;

import com.pcProject.ecomOrderService.model.OrderDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderDetails,Integer> {
    OrderDetails findByProductName(String productName);
    OrderDetails findByOrderId(Integer id);
    List<OrderDetails> findAllByProductName(String productName);
    List<OrderDetails> findAllByUserName(String userName);
    List<OrderDetails> findAllByUserNameAndProductName(String userName, String productName);
}
