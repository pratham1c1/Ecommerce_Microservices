package com.pcProject.ecomOrderService.service;

import com.netflix.discovery.converters.Auto;
import com.pcProject.ecomOrderService.Repository.OrderRepository;
import com.pcProject.ecomOrderService.feignRepository.EcomProductService;
import com.pcProject.ecomOrderService.feignRepository.EcomUserService;
import com.pcProject.ecomOrderService.model.OrderDetails;
import com.pcProject.ecomOrderService.model.ProductWrapper;
import com.pcProject.ecomOrderService.model.UserProducts;
import com.pcProject.ecomOrderService.model.UserProductsResponse;
import jakarta.persistence.criteria.Order;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderDetailsService {

    @Autowired
    private EcomUserService ecomUserService;
    @Autowired
    private EcomProductService ecomProductService;
    @Autowired
    private OrderRepository orderRepo;


    public ResponseEntity<UserProductsResponse<List<OrderDetails>>> getAllOrderDetails(String userName){
        UserProductsResponse<List<String>> response = ecomUserService.getAllUserProducts(userName).getBody();
        List<OrderDetails> orderList = new ArrayList<>();

        assert response != null;
        if(response.getStatus() == 200){
            orderList.addAll(orderRepo.findAllByUserName(userName));
            return new ResponseEntity<>(new UserProductsResponse<>(orderList,HttpStatus.OK.value(), "Details retrieved successfully"),HttpStatus.OK);
        }

        return new ResponseEntity<>(new UserProductsResponse<>(orderList,HttpStatus.BAD_REQUEST.value(), "Something went wrong!"),HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<UserProductsResponse<OrderDetails>> addOrderDetails(UserProducts product){
        // To check if Product exists
        UserProductsResponse<ProductWrapper> validateProduct = ecomProductService.validateProduct(new ProductWrapper(product.getProductName())).getBody();

        UserProductsResponse<OrderDetails> orderResponse = new UserProductsResponse<OrderDetails>();;
        UserProductsResponse<UserProducts> serviceResponse ;

        // Validate Product
        assert validateProduct != null;
        if(validateProduct.getStatus() != 200){
            OrderDetails newOrder = new OrderDetails(-1,null,null,null);
            newOrder.setPaymentStatus(null);
            orderResponse.setData(newOrder);
            orderResponse.setStatus(validateProduct.getStatus());
            orderResponse.setMessage(validateProduct.getMessage());
            return new ResponseEntity<>(orderResponse, HttpStatus.BAD_REQUEST);
        }

        // Get the Added User Products
        serviceResponse = ecomUserService.addUserProduct(product).getBody();

        //Save the new Order as Placed
        assert serviceResponse != null;
        if(serviceResponse.getStatus() == 200){
            OrderDetails newOrder = new OrderDetails();
            newOrder.setUserName(serviceResponse.getData().getUserName());
            newOrder.setProductName(serviceResponse.getData().getProductName());
            newOrder.setOrderStatus("Placed");
            orderRepo.save(newOrder);

            orderResponse.setData(newOrder);
            orderResponse.setStatus(HttpStatus.OK.value());
            orderResponse.setMessage("Order Placed successfully");

            return new ResponseEntity<>(orderResponse,HttpStatus.OK);
        }

        // Case if User doesn't exist
        if(serviceResponse.getData() == null){
            orderResponse.setData(new OrderDetails(-1,null,null,null));
            orderResponse.setMessage("User doesn't exists");
            orderResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        }
        return new ResponseEntity<>(orderResponse,HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<UserProductsResponse<UserProducts>> deleteOrderDetails(UserProducts userProduct){
//        UserProductsResponse<ProductWrapper> validateProduct = ecomProductService.validateProduct(new ProductWrapper(product.getProductName())).getBody();
//        UserProductsResponse<UserProducts> response ;
//
//        assert validateProduct != null;
//        if(validateProduct.getStatus() != 200){
//            response = new UserProductsResponse<UserProducts>();
//            response.setData(new UserProducts(null,null));
//            response.setStatus(HttpStatus.BAD_REQUEST.value());
//            response.setMessage("Product doesn't exists");
//            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//        }

        // Validate User Product
        UserProductsResponse<UserProducts> response = new UserProductsResponse<>();
        UserProductsResponse<UserProducts> userResponse = ecomUserService.validateUserProduct(userProduct).getBody();
        assert userResponse != null;
        if(userResponse.getStatus() != 200){
            response.setData(new UserProducts(null,null));
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMessage("User doesn't exists");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        // Remove the Product from User List
        response = ecomUserService.removeUserProduct(userProduct).getBody();
        assert response != null;
        if(response.getStatus() == 200) {
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        if(response.getData() == null){
            response.setData(new UserProducts(null,null));
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMessage("User doesn't exists");
            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);

        }

        return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
    }

    // Get First Unpaid Order of User Product
    public ResponseEntity<UserProductsResponse<OrderDetails>> getOneOrderDetails(UserProducts userProduct){
        UserProductsResponse<OrderDetails> response = new UserProductsResponse<>();

        UserProductsResponse<UserProducts> userResponse = ecomUserService.validateUserProduct(userProduct).getBody();
        assert userResponse != null;
        if(userResponse.getStatus() != 200){
            response.setData(new OrderDetails(-1,null,null,null,null));
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMessage("User doesn't exists");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        // Get First Unpaid Order
        for(OrderDetails order : orderRepo.findAllByUserNameAndProductName(userProduct.getUserName(), userProduct.getProductName())){
            if(order.getPaymentStatus().equals("Unpaid")) {
                response.setData(order);
                response.setMessage("Successfully retrieved User product details");
                response.setStatus(HttpStatus.OK.value());
                return new ResponseEntity<>(response,HttpStatus.OK);
            }
        }

        response.setMessage("No Such Order is available");
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
    }

    // Update Payment Status
    @Transactional
    public ResponseEntity<UserProductsResponse<OrderDetails>> updatePaymentStatus(UserProducts userProduct){
        UserProductsResponse<OrderDetails> response = new UserProductsResponse<>();

        UserProductsResponse<UserProducts> userResponse = ecomUserService.validateUserProduct(userProduct).getBody();
        assert userResponse != null;
        if(userResponse.getStatus() != 200){
            response.setData(new OrderDetails(-1,null,null,null,null));
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMessage("User doesn't exists");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        for(OrderDetails order : orderRepo.findAllByUserNameAndProductName(userProduct.getUserName(), userProduct.getProductName())){
            if(order.getPaymentStatus().equals("Unpaid")){
                order.setPaymentStatus("Paid");
                orderRepo.save(order);
                response.setData(order);
                break;
            }
        }

        response.setMessage("Successfully updated Product payment status");
        response.setStatus(HttpStatus.OK.value());
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<OrderDetails> updateOrderStatus(int orderId) {
        OrderDetails userOrder = orderRepo.findById(orderId).get();

        if(userOrder.getOrderStatus().equals("Placed")) {
            if(userOrder.getPaymentStatus().equals("Paid"))
                userOrder.setOrderStatus("Shipped");
            else
                return new ResponseEntity<>(userOrder,HttpStatus.BAD_REQUEST);
        }
        else{
            userOrder.setOrderStatus("Delivered");
        }
        orderRepo.save(userOrder);
        if(userOrder.getOrderStatus().equals("Delivered")){
            UserProducts products = new UserProducts(userOrder.getUserName(), userOrder.getProductName());
            orderRepo.delete(userOrder);
            ecomUserService.removeUserProduct(products);
        }

        return new ResponseEntity<>(userOrder,HttpStatus.OK);
    }
}