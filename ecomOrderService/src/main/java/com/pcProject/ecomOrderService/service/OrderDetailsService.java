package com.pcProject.ecomOrderService.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pcProject.ecomOrderService.Repository.OrderRepository;
import com.pcProject.ecomOrderService.feignRepository.EcomProductService;
import com.pcProject.ecomOrderService.feignRepository.EcomUserService;
import com.pcProject.ecomOrderService.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class OrderDetailsService {

    @Autowired
    private EcomUserService ecomUserService;
    @Autowired
    private EcomProductService ecomProductService;
    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    @Qualifier("userProductKafkaTemplate")
    private KafkaTemplate<String,Object> userProductKafkaTemplate;

    public OrderDetailsService(KafkaTemplate<String,Object> template){
        this.userProductKafkaTemplate = template;
    }

    public ResponseEntity<UserProductsResponse<List<OrderDetails>>> getAllOrderDetails(String userName){
        UserProductsResponse<List<String>> response = ecomUserService.getAllUserProducts(userName).getBody();
        List<OrderDetails> orderList = new ArrayList<>();

        if(response == null){
            log.error("Error : The response from ecomUserService is null");
            return new ResponseEntity<>(new UserProductsResponse<>(null,HttpStatus.INTERNAL_SERVER_ERROR.value(), "Something went wrong !"),HttpStatus.BAD_REQUEST);
        }
        if(response.getStatus() == 200){
            orderList.addAll(orderRepo.findAllByUserName(userName));
            log.info("User Validation : {}",response);
            return new ResponseEntity<>(new UserProductsResponse<>(orderList,HttpStatus.OK.value(), "Details retrieved successfully"),HttpStatus.OK);
        }

        return new ResponseEntity<>(new UserProductsResponse<>(orderList,HttpStatus.BAD_REQUEST.value(), "Something went wrong!"),HttpStatus.BAD_REQUEST);
    }

    @Transactional
    @KafkaListener(topics = "ecomUserService_addUserProduct")
    public ResponseEntity<UserProductsResponse<OrderDetails>> addOrderDetailsConsumer(String userProductResponse) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        log.info("Fetching the order Details from ecomUserService : {}",userProductResponse);
        UserProductsResponse<OrderDetailsWrapper> ecomUserServiceResponse = objectMapper.readValue(userProductResponse, new TypeReference<UserProductsResponse<OrderDetailsWrapper>>() {});

        OrderDetailsWrapper orderDetails = ecomUserServiceResponse.getData();
        UserProductsResponse<OrderDetails> orderResponse = new UserProductsResponse<OrderDetails>();
        log.info("Fetched the data successfully : {} ", orderDetails);
        // Placing the order in OrderRepo
        log.info("Placing the Order");
        OrderDetails existingOrderDetails = orderRepo.findByOrderId(orderDetails.getOrderId());
        existingOrderDetails.setOrderStatus("Placed");
        orderRepo.save(existingOrderDetails);
        log.info("Order placed successfully : {}",existingOrderDetails);

        orderResponse.setData(existingOrderDetails);
        orderResponse.setStatus(HttpStatus.OK.value());
        orderResponse.setMessage("Order Placed successfully");

        return new ResponseEntity<>(orderResponse,HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<UserProductsResponse<OrderDetails>> addOrderDetails(UserProducts product){
        // To check if Product exists
        log.info("Sending to ecomProductService to consume the product : {}",product);
        UserProductsResponse<ProductWrapper> validateProduct = ecomProductService.consumeProduct(new ProductWrapper(product.getProductName())).getBody();

        UserProductsResponse<OrderDetails> orderResponse = new UserProductsResponse<OrderDetails>();
        UserProductsResponse<UserProducts> serviceResponse ;

        // Validate Product
        if(validateProduct == null){
            log.error("Error : product is not valid from ecomProductService");
            orderResponse.setData(null);
            orderResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            orderResponse.setMessage("product is not valid from ecomProductService");
            return new ResponseEntity<>(orderResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if(validateProduct.getStatus() != 200){
            OrderDetails newOrder = new OrderDetails(-1,null,null,null);
            newOrder.setPaymentStatus(null);
            orderResponse.setData(newOrder);
            orderResponse.setStatus(validateProduct.getStatus());
            orderResponse.setMessage(validateProduct.getMessage());
            return new ResponseEntity<>(orderResponse, HttpStatus.BAD_REQUEST);
        }
        log.info("Successfully Consumed the Product from ecomProductService {}",validateProduct);

        //Validate User
        log.info("Sending to ecomUserService to validate the user : {}",product.getUserName());
        serviceResponse = ecomUserService.validateUser(product).getBody();
        // Case if User doesn't exist
        if(serviceResponse == null){
            log.error("Something went wrong while adding Product to user");
            orderResponse.setData(null);
            orderResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            orderResponse.setMessage("Something went wrong while adding Product to user");
            return new ResponseEntity<>(orderResponse, HttpStatus.BAD_REQUEST);
        }
        if(serviceResponse.getData() == null){
            orderResponse.setData(new OrderDetails(-1,null,null,null));
            orderResponse.setMessage("User doesn't exists");
            orderResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            return new ResponseEntity<>(orderResponse,HttpStatus.BAD_REQUEST);
        }

        log.info("User Validation Successful : {}", product.getUserName());

        log.info("Processing order Details ");
        // Add order Details to OrderRepo
        OrderDetails newOrder = new OrderDetails();
        newOrder.setUserName(serviceResponse.getData().getUserName());
        newOrder.setProductName(product.getProductName());
        newOrder.setOrderStatus("Waiting_to_Place");
        newOrder.setPaymentStatus("UnPaid");
        orderRepo.save(newOrder);

        log.info("Order processes successfully");
        log.info("Sending to ecomUserService append to userList : {}",newOrder);
        //Sending to ecomUserService to append to user products
        userProductKafkaTemplate.send("ecomOrderService_addUserProduct",new OrderDetailsWrapper(newOrder.getOrderId(), newOrder.getUserName(), newOrder.getProductName()));

        orderResponse.setData(newOrder);
        orderResponse.setStatus(HttpStatus.OK.value());
        orderResponse.setMessage("Order received");
        return new ResponseEntity<>(orderResponse, HttpStatus.OK);

    }

    @Transactional
    public ResponseEntity<UserProductsResponse<OrderDetailsWrapper>> deleteOrderDetails(OrderDetailsWrapper orderDetailsWrapper){
        log.info("Received details to delete the order");
        UserProductsResponse<OrderDetailsWrapper> response = new UserProductsResponse<>();

        // Validate the Order
        OrderDetails orderDetails = orderRepo.findByOrderId(orderDetailsWrapper.getOrderId());
        response.setData(orderDetailsWrapper);

        if(orderDetails == null){
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMessage("Details doesn't match");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        else if(!orderDetails.getOrderStatus().equals("Placed")){
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMessage("Order already processed");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        log.info("Validated the details successfully {}", orderDetailsWrapper);

        // Validate User Product
        UserProductsResponse<UserProducts> userResponse = ecomUserService.validateUserProduct(new UserProducts(orderDetailsWrapper.getUserName(), orderDetailsWrapper.getProductName())).getBody();
        if(userResponse == null){
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Something went wrong !");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if(userResponse.getStatus() != 200){
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMessage("Details doesn't match");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        log.info("Sending to ecomUserService to remove from userList {}",orderDetailsWrapper);
        userProductKafkaTemplate.send("ecomOrderService_removeUserProduct", orderDetailsWrapper);

        log.info("Sending to ecomProductService to Preserve the product {}",orderDetailsWrapper);
        userProductKafkaTemplate.send("ecomOrderService_addToProductQuantity", orderDetailsWrapper);

        orderRepo.deleteById(orderDetailsWrapper.getOrderId());
        log.info("Order details successfully deleted");

        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    // Get First Unpaid Order of User Product
    public ResponseEntity<UserProductsResponse<OrderDetails>> getOneOrderDetails(UserProducts userProduct){
        UserProductsResponse<OrderDetails> response = new UserProductsResponse<>();

        UserProductsResponse<UserProducts> userResponse = ecomUserService.validateUserProduct(userProduct).getBody();
        if(userResponse == null){
            response.setData(null);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Something went wrong !");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
        if(userResponse == null){
            response.setData(null);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Something went wrong !");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if(userResponse.getStatus() != 200){
            response.setData(new OrderDetails(-1,null,null,null,null));
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMessage("User doesn't exists");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        for(OrderDetails order : orderRepo.findAllByUserNameAndProductName(userProduct.getUserName(), userProduct.getProductName()))
            if (order.getPaymentStatus().equals("Unpaid")) {
                order.setPaymentStatus("Paid");
                orderRepo.save(order);
                response.setData(order);
                break;
            }

        response.setMessage("Successfully updated Product payment status");
        response.setStatus(HttpStatus.OK.value());
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<UserProductsResponse<OrderDetails>> updateOrderStatus(int orderId) {
        OrderDetails userOrder = orderRepo.findById(orderId).get();
        UserProductsResponse<OrderDetails> response = new UserProductsResponse<>();

        if(userOrder.getOrderStatus().equals("Delivered")){
            UserProducts products = new UserProducts(userOrder.getUserName(), userOrder.getProductName());
            orderRepo.delete(userOrder);
            OrderDetailsWrapper orderDetailsWrapper = new OrderDetailsWrapper(userOrder.getOrderId(), userOrder.getUserName(), userOrder.getProductName());

            log.info("Sending to ecomUserService to remove from userList {}",orderDetailsWrapper);
            userProductKafkaTemplate.send("ecomOrderService_removeUserProduct", orderDetailsWrapper);

        }
        else if(userOrder.getOrderStatus().equals("Placed")) {
            if(userOrder.getPaymentStatus().equals("Paid")){
                userOrder.setOrderStatus("Shipped");
                orderRepo.save(userOrder);
                response.setData(userOrder);
                response.setStatus(200);
                response.setMessage("Order Status Updated Successfully !");
            }
            else{
                response.setData(userOrder);
                response.setStatus(402);
                response.setMessage("Payment is due !");
                return new ResponseEntity<>(response,HttpStatus.OK);
            }
        }
        else{
            userOrder.setOrderStatus("Delivered");
            orderRepo.save(userOrder);
            response.setData(userOrder);
            response.setStatus(200);
            response.setMessage("Successfully updated the order Status");
        }

        return new ResponseEntity<>(response,HttpStatus.OK);
    }
}