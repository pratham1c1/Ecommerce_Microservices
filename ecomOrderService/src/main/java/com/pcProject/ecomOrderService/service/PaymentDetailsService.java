package com.pcProject.ecomOrderService.service;

import com.pcProject.ecomOrderService.feignRepository.EcomProductService;
import com.pcProject.ecomOrderService.feignRepository.EcomUserService;
import com.pcProject.ecomOrderService.model.OrderDetails;
import com.pcProject.ecomOrderService.model.ProductWrapper;
import com.pcProject.ecomOrderService.model.UserProducts;
import com.pcProject.ecomOrderService.model.UserProductsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentDetailsService {

    @Autowired
    private EcomUserService ecomUserService;
    @Autowired
    private EcomProductService ecomProductService;
    @Autowired
    private OrderDetailsService orderDetailsService;

    public ResponseEntity<UserProductsResponse<String>> getAllPayment(String userName){
        UserProductsResponse<List<String>> response = ecomUserService.getAllUserProducts(userName).getBody();

        if(response == null){
            return new ResponseEntity<>(new UserProductsResponse<>(null,500,"Something went wrong !"),HttpStatus.BAD_REQUEST);
        }
        if(response.getStatus() != 200){
            return new ResponseEntity<>(new UserProductsResponse<String>(null,response.getStatus(),response.getMessage()),HttpStatus.BAD_REQUEST);
        }

        int totalPayment = 0;

        // To count all Product values
        for(String productName : response.getData()){
            ResponseEntity<UserProductsResponse<String>> productValueResponse = ecomProductService.getProductValue(new ProductWrapper(productName));
            if(productValueResponse != null && productValueResponse.getBody() != null){
                totalPayment += Integer.parseInt(productValueResponse.getBody().getData());
            }
        }

        return new ResponseEntity<>(new UserProductsResponse<String>(Integer.toString(totalPayment),response.getStatus(),response.getMessage()),HttpStatus.OK);
    }

    public ResponseEntity<UserProductsResponse<UserProducts>> OneProductPayment(UserProducts userProduct){
        // Get All Products from User Order list
        UserProductsResponse<List<String>> response = ecomUserService.getAllUserProducts(userProduct.getUserName()).getBody();
        if(response == null){
            return new ResponseEntity<>(new UserProductsResponse<>(null,500,"Something went wrong !"),HttpStatus.BAD_REQUEST);
        }
        if(response.getStatus() != 200){
            return new ResponseEntity<>(new UserProductsResponse<UserProducts>(null,response.getStatus(),response.getMessage()),HttpStatus.BAD_REQUEST);
        }

        //Check if product available in User Order list
        if(!response.getData().contains(userProduct.getProductName())){
            return new ResponseEntity<>(new UserProductsResponse<UserProducts>(null,response.getStatus(),"Product is not available in Order List"), HttpStatus.BAD_REQUEST);
        }

        //Get the first Unpaid Order of User Product
        UserProductsResponse<OrderDetails> userOrderResponse = orderDetailsService.getOneOrderDetails(userProduct).getBody();
        if(userOrderResponse.getStatus() != 200){
            return new ResponseEntity<>(new UserProductsResponse<UserProducts>(null,response.getStatus(),userOrderResponse.getMessage()), HttpStatus.BAD_REQUEST);
        }

        // Update Payment status to 'Paid'
        ResponseEntity<UserProductsResponse<OrderDetails>> paymentStatusResponse = orderDetailsService.updatePaymentStatus(userProduct);
        if(paymentStatusResponse.getStatusCode().value() != 200){
            return new ResponseEntity<>(new UserProductsResponse<UserProducts>(null,paymentStatusResponse.getStatusCode().value(),"Something went wrong while updating status!"),HttpStatus.BAD_REQUEST);
        }

        //Update Order status
        ResponseEntity<UserProductsResponse<OrderDetails>> orderStatusResponse = orderDetailsService.updateOrderStatus(userOrderResponse.getData().getOrderId());
        if(orderStatusResponse.getStatusCode().value() != 200){
            return new ResponseEntity<>(new UserProductsResponse<UserProducts>(null,response.getStatus(),"Payment is due for the User Product."), HttpStatus.BAD_REQUEST);
        }


        return new ResponseEntity<>(new UserProductsResponse<>(userProduct,response.getStatus(),"Payment Successful"),HttpStatus.OK);
    }

    public ResponseEntity<UserProductsResponse<String>> settleAllPayment(String userName){
        UserProductsResponse<List<String>> response = ecomUserService.getAllUserProducts(userName).getBody();

        if(response == null){
            return new ResponseEntity<>(new UserProductsResponse<>(null,500,"Something went wrong !"),HttpStatus.BAD_REQUEST);
        }
        if(response.getStatus() != 200){
            return new ResponseEntity<>(new UserProductsResponse<String>(null,response.getStatus(),response.getMessage()),HttpStatus.BAD_REQUEST);
        }

        int totalPayment = 0;
        for(String productName : response.getData()){
            ResponseEntity<UserProductsResponse<String>> productValueResponse = ecomProductService.getProductValue(new ProductWrapper(productName));
            ecomUserService.removeUserProduct(new UserProducts(userName,productName));

            if(productValueResponse == null || productValueResponse.getBody() == null){
                response.setData(null);
                response.setStatus(500);
                response.setMessage("Something went wrong !");
                return new ResponseEntity<>(new UserProductsResponse<String>(null,response.getStatus(),response.getMessage()),HttpStatus.BAD_REQUEST);
            }
            totalPayment += Integer.parseInt(productValueResponse.getBody().getData());
        }

        response.setMessage("Payment settled");

        return new ResponseEntity<>(new UserProductsResponse<String>(Integer.toString(totalPayment),response.getStatus(),response.getMessage()),HttpStatus.OK);
    }
}