package com.pcProject.ecomOrderService.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pcProject.ecomOrderService.Repository.OrderRepository;
import com.pcProject.ecomOrderService.feignRepository.EcomProductService;
import com.pcProject.ecomOrderService.feignRepository.EcomUserService;
import com.pcProject.ecomOrderService.model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

class OrderDetailsServiceTest {

    @Mock
    private EcomProductService ecomProductService;
    @Mock
    private EcomUserService ecomUserService;
    @Mock
    private OrderRepository orderRepo;
    @Mock
    private OrderDetails existingOrderDetails;
    @Mock
    private KafkaTemplate<String, OrderDetailsWrapper> userProductKafkaTemplate;
    @InjectMocks
    private OrderDetailsService orderDetailsService;

    private static final String TEST_USER = "test-userName";
    private static final String TEST_PRODUCT = "test-productName";
    private OrderDetailsWrapper sampleWrapper;
    private OrderDetails sampleOrder;
    private UserProducts sampleProduct;

    @Test
    @BeforeEach
    void configureMockObjects(){
        MockitoAnnotations.openMocks(this);
        sampleProduct = new UserProducts();
        sampleProduct.setProductName(TEST_PRODUCT);
        sampleProduct.setUserName(TEST_USER);
        sampleWrapper = new OrderDetailsWrapper(101, TEST_USER, TEST_PRODUCT);

        sampleOrder = new OrderDetails();
        sampleOrder.setOrderId(101);
        sampleOrder.setUserName(TEST_USER);
        sampleOrder.setProductName(TEST_PRODUCT);
        sampleOrder.setOrderStatus("Placed");
        sampleOrder.setPaymentStatus("Unpaid");
    }

    // getAllOrderDetails
    @Test
    void getAllOrderDetails_WhereResponseStatusIs200_ShouldReturn200() {
        // 1. Response is null case
        String userName = "test-user";
        List<String> userOrders = Arrays.asList("dummyProduct1","dummyProduct2");
        UserProductsResponse<List<String>> testResponseData = new UserProductsResponse<>(userOrders,200,"Successfully fetched the details");

        ResponseEntity<UserProductsResponse<List<String>>> mockResponseEntity = new ResponseEntity<>(testResponseData, HttpStatus.OK);

        when(ecomUserService.getAllUserProducts(userName)).thenReturn(mockResponseEntity);

        ResponseEntity<UserProductsResponse<List<OrderDetails>>> methodResponse = orderDetailsService.getAllOrderDetails(userName);

        assertEquals(HttpStatus.OK, methodResponse.getStatusCode());
    }
    @Test
    void getAllOrderDetails_WhereResponseStatusIsNot200_ShouldReturnBadRequest(){
        String userName = "test-user";
        List<String> userOrders = Arrays.asList("dummyProduct1","dummyProduct2");
        UserProductsResponse<List<String>> testResponseDataWithNotOKStatus = new UserProductsResponse<>(userOrders,201,"Something went wrong");
        ResponseEntity<UserProductsResponse<List<String>>> mockResponseEntity = new ResponseEntity<>(testResponseDataWithNotOKStatus, HttpStatus.OK);

        when(ecomUserService.getAllUserProducts(userName)).thenReturn(mockResponseEntity);

        ResponseEntity<UserProductsResponse<List<OrderDetails>>> methodResponse = orderDetailsService.getAllOrderDetails(userName);

        assertEquals(HttpStatus.BAD_REQUEST.value(), Objects.requireNonNull(methodResponse.getBody()).getStatus());
    }
    @Test
    void getAllOrderDetails_WhereResponseIsNull_ShouldReturnInternalServerError() {
        // 1. Response is null case

        String userName = "test-user";
        List<String> userOrders = Arrays.asList("dummyProduct1","dummyProduct2");
        UserProductsResponse<List<String>> nullTestResponseData = null;
        ResponseEntity<UserProductsResponse<List<String>>> mockResponseEntity = new ResponseEntity<>(nullTestResponseData, HttpStatus.OK);

        when(ecomUserService.getAllUserProducts(userName)).thenReturn(mockResponseEntity);

        ResponseEntity<UserProductsResponse<List<OrderDetails>>> methodResponse = orderDetailsService.getAllOrderDetails(userName);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), Objects.requireNonNull(methodResponse.getBody()).getStatus());

        // 2. Response status is 200
    }
//-------------------------------------
    //addOrderDetailsConsumer
    @Test
    void addOrderDetailsConsumer_WhenUserProductStringIsImproperJSON_ShouldThrowJsonProcessingException(){
        String userProductResponse = "{\"data\": {\"orderId\": 101, \"userName\":\"test-user\",\"productName\":\"test-product\"}, " +
                "\"status\": 200" +
                "\"message\":\"Successfully added userProducts\"}";
        int orderId = 101;
        OrderDetails orderDetails = new OrderDetails(101,"test-user","test-product","Placed","Unpaid");

        JsonProcessingException exception = assertThrows(JsonProcessingException.class, ()->orderDetailsService.addOrderDetailsConsumer(userProductResponse));
        assertEquals("Unexpected character ('\"' (code 34)): was expecting comma to separate Object entries", exception.getOriginalMessage());
    }
    @Test
    void addOrderDetailsConsumer_WhenUserProductStringIsProperJSON_ShouldReturn200() throws JsonProcessingException {
        String userProductResponse = "{\"data\": {\"orderId\": 101, \"userName\":\"test-user\",\"productName\":\"test-product\"},"+
	"\"status\": 200,"+
    "\"message\":\"Successfully added userProducts\"}";
        int orderId = 101;
        OrderDetails existingOrderDetails = new OrderDetails(101,"test-user","test-product","Placed","Unpaid");


        when(orderRepo.findByOrderId(orderId)).thenReturn(existingOrderDetails);
        when(orderRepo.save(existingOrderDetails)).thenReturn(existingOrderDetails);

        ResponseEntity<?> response  = orderDetailsService.addOrderDetailsConsumer(userProductResponse);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
//-------------------------------------
    //addOrderDetails
    @Test
    void addOrderDetails_WhenValidateProductIsNull_ShouldReturnInternalServerError() {
        // Mocking body() to return null
        ResponseEntity<UserProductsResponse<ProductWrapper>> mockResp = mock(ResponseEntity.class);
        when(mockResp.getBody()).thenReturn(null);
        when(ecomProductService.consumeProduct(any())).thenReturn(mockResp);

        ResponseEntity<UserProductsResponse<OrderDetails>> response = orderDetailsService.addOrderDetails(sampleProduct);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, Objects.requireNonNull(response.getBody()).getStatus());
        assertEquals("product is not valid from ecomProductService", response.getBody().getMessage());
    }
    @Test
    void addOrderDetails_WhenProductStatusNot200_ShouldReturnBadRequest() {
        UserProductsResponse<ProductWrapper> mockBody = new UserProductsResponse<>();
        mockBody.setStatus(404);
        mockBody.setMessage("Product not found");

        ResponseEntity<UserProductsResponse<ProductWrapper>> mockResp = ResponseEntity.ok(mockBody);
        when(ecomProductService.consumeProduct(any())).thenReturn(mockResp);

        ResponseEntity<UserProductsResponse<OrderDetails>> response = orderDetailsService.addOrderDetails(sampleProduct);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(404, Objects.requireNonNull(response.getBody()).getStatus());
        assertEquals(-1, response.getBody().getData().getOrderId());
    }
    @Test
    void addOrderDetails_WhenUserServiceResponseIsNull_ShouldReturnBadRequest() {
        // Setup Product Pass
        UserProductsResponse<ProductWrapper> pBody = new UserProductsResponse<>();
        pBody.setStatus(200);
        when(ecomProductService.consumeProduct(any())).thenReturn(ResponseEntity.ok(pBody));

        // Setup User Null
        ResponseEntity<UserProductsResponse<UserProducts>> uResp = mock(ResponseEntity.class);
        when(uResp.getBody()).thenReturn(null);
        when(ecomUserService.validateUser(any())).thenReturn(uResp);

        ResponseEntity<UserProductsResponse<OrderDetails>> response = orderDetailsService.addOrderDetails(sampleProduct);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(500, Objects.requireNonNull(response.getBody()).getStatus());
    }
    @Test
    void addOrderDetails_WhenUserDoesNotExist_ShouldReturnBadRequest() {
        // Setup Product Pass
        UserProductsResponse<ProductWrapper> pBody = new UserProductsResponse<>();
        pBody.setStatus(200);
        when(ecomProductService.consumeProduct(any())).thenReturn(ResponseEntity.ok(pBody));

        // Setup User Data Null
        UserProductsResponse<UserProducts> uBody = new UserProductsResponse<>();
        uBody.setData(null);
        when(ecomUserService.validateUser(any())).thenReturn(ResponseEntity.ok(uBody));

        ResponseEntity<UserProductsResponse<OrderDetails>> response = orderDetailsService.addOrderDetails(sampleProduct);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("User doesn't exists", Objects.requireNonNull(response.getBody()).getMessage());
    }
    @Test
    void addOrderDetails_WhenSuccess_ShouldSaveAndSendKafkaMessage() {
        // Setup Product Pass
        UserProductsResponse<ProductWrapper> pBody = new UserProductsResponse<>();
        pBody.setStatus(200);
        when(ecomProductService.consumeProduct(any())).thenReturn(ResponseEntity.ok(pBody));

        // Setup User Pass
        UserProducts user = new UserProducts();
        user.setUserName("test-user");
        UserProductsResponse<UserProducts> uBody = new UserProductsResponse<>();
        uBody.setData(user);

        when(ecomUserService.validateUser(any())).thenReturn(ResponseEntity.ok(uBody));
        // Mock Repo Save
        when(orderRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        ResponseEntity<UserProductsResponse<OrderDetails>> response = orderDetailsService.addOrderDetails(sampleProduct);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Order received", response.getBody().getMessage());

        // Verify Interactions
        verify(orderRepo, times(1)).save(any(OrderDetails.class));
        verify(userProductKafkaTemplate, times(1)).send(eq("ecomOrderService_addUserProduct"), any());
    }
//-------------------------------------
    //deleteOrderDetails
    @Test
    void deleteOrderDetails_WhenOrderNotFound_ShouldReturnBadRequest() {
    when(orderRepo.findByOrderId(101)).thenReturn(null);

    ResponseEntity<UserProductsResponse<OrderDetailsWrapper>> response = orderDetailsService.deleteOrderDetails(sampleWrapper);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("Details doesn't match", response.getBody().getMessage());
}
    @Test
    void deleteOrderDetails_WhenOrderNotPlaced_ShouldReturnBadRequest() {
        sampleOrder.setOrderStatus("Shipped");
        when(orderRepo.findByOrderId(101)).thenReturn(sampleOrder);

        ResponseEntity<UserProductsResponse<OrderDetailsWrapper>> response = orderDetailsService.deleteOrderDetails(sampleWrapper);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Order already processed", response.getBody().getMessage());
    }
    @Test
    void deleteOrderDetails_WhenSuccess_ShouldSendKafkaAndReturnOk() {
        when(orderRepo.findByOrderId(101)).thenReturn(sampleOrder);

        UserProductsResponse<UserProducts> uBody = new UserProductsResponse<>();
        uBody.setStatus(200);
        when(ecomUserService.validateUserProduct(any())).thenReturn(ResponseEntity.ok(uBody));

        ResponseEntity<UserProductsResponse<OrderDetailsWrapper>> response = orderDetailsService.deleteOrderDetails(sampleWrapper);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userProductKafkaTemplate, times(2)).send(anyString(), any(OrderDetailsWrapper.class));
        verify(orderRepo).deleteById(101);
    }
    @Test
    void deleteOrderDetails_WhenSuccess_ButServiceResponseIsNull_ShouldReturnInternalServerError() {
        when(orderRepo.findByOrderId(101)).thenReturn(sampleOrder);

        UserProductsResponse<UserProducts> uBody = new UserProductsResponse<>();
        uBody.setStatus(200);
        ResponseEntity<UserProductsResponse<UserProducts>> mockResponseEntity = mock(ResponseEntity.class);

        when(mockResponseEntity.getBody()).thenReturn(null);
        when(ecomUserService.validateUserProduct(any())).thenReturn(mockResponseEntity);

        ResponseEntity<UserProductsResponse<OrderDetailsWrapper>> response = orderDetailsService.deleteOrderDetails(sampleWrapper);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Something went wrong !", Objects.requireNonNull(response.getBody()).getMessage());
    }
    @Test
    void deleteOrderDetails_WhenSuccess_ButServiceResponseStatusIsNot200_ShouldReturnBadRequest() {
        when(orderRepo.findByOrderId(101)).thenReturn(sampleOrder);

        UserProductsResponse<UserProducts> uBody = new UserProductsResponse<>();
        uBody.setStatus(400); // Status is not 200

        when(ecomUserService.validateUserProduct(any())).thenReturn(ResponseEntity.ok(uBody));

        ResponseEntity<UserProductsResponse<OrderDetailsWrapper>> response = orderDetailsService.deleteOrderDetails(sampleWrapper);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Details doesn't match", Objects.requireNonNull(response.getBody()).getMessage());
    }
//-------------------------------------
    //getOneOrderDetails
    @Test
    void getOneOrderDetails_WhenUserInvalid_ShouldReturnBadRequest() {
    UserProducts up = new UserProducts(TEST_USER, TEST_PRODUCT);
    UserProductsResponse<UserProducts> uBody = new UserProductsResponse<>();
    uBody.setStatus(400);
    when(ecomUserService.validateUserProduct(any())).thenReturn(ResponseEntity.ok(uBody));

    ResponseEntity<UserProductsResponse<OrderDetails>> response = orderDetailsService.getOneOrderDetails(up);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals(-1, response.getBody().getData().getOrderId());
}
    @Test
    void getOneOrderDetails_WhenServiceResponseIsNull_ShouldReturnInternalServerError() {
        UserProducts up = new UserProducts(TEST_USER, TEST_PRODUCT);
        UserProductsResponse<UserProducts> uBody = new UserProductsResponse<>();
        uBody.setStatus(400);
        ResponseEntity<UserProductsResponse<UserProducts>> mockResponsEntity = mock(ResponseEntity.class);

        when(mockResponsEntity.getBody()).thenReturn(null);
        when(ecomUserService.validateUserProduct(any())).thenReturn(mockResponsEntity);

        ResponseEntity<UserProductsResponse<OrderDetails>> response = orderDetailsService.getOneOrderDetails(up);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody().getData());
    }
    @Test
    void getOneOrderDetails_WhenUnpaidOrderExistsWithUnpaidPaymentStatus_ShouldReturnOk() {
        UserProducts up = new UserProducts(TEST_USER, TEST_PRODUCT);
        UserProductsResponse<UserProducts> uBody = new UserProductsResponse<>();
        uBody.setStatus(200);
        when(ecomUserService.validateUserProduct(any())).thenReturn(ResponseEntity.ok(uBody));

        when(orderRepo.findAllByUserNameAndProductName(TEST_USER, TEST_PRODUCT))
                .thenReturn(Arrays.asList(sampleOrder));

        ResponseEntity<UserProductsResponse<OrderDetails>> response = orderDetailsService.getOneOrderDetails(up);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Unpaid", response.getBody().getData().getPaymentStatus());
    }
    @Test
    void getOneOrderDetails_WhenUnpaidOrderNotExists_ShouldReturnBadRequset() {
        UserProducts up = new UserProducts(TEST_USER, TEST_PRODUCT);
        UserProductsResponse<UserProducts> uBody = new UserProductsResponse<>();
        uBody.setStatus(200);
        sampleOrder.setPaymentStatus("Paid");

        when(ecomUserService.validateUserProduct(any())).thenReturn(ResponseEntity.ok(uBody));
        when(orderRepo.findAllByUserNameAndProductName(TEST_USER, TEST_PRODUCT))
                .thenReturn(List.of(sampleOrder));

        ResponseEntity<UserProductsResponse<OrderDetails>> response = orderDetailsService.getOneOrderDetails(up);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("No Such Order is available", response.getBody().getMessage());
    }
    @Test
    void getOneOrderDetails_WhenOrderListIsEmpty_ShouldReturnBadRequset() {
        UserProducts up = new UserProducts(TEST_USER, TEST_PRODUCT);
        UserProductsResponse<UserProducts> uBody = new UserProductsResponse<>();
        uBody.setStatus(200);
        sampleOrder.setPaymentStatus("Paid");

        when(ecomUserService.validateUserProduct(any())).thenReturn(ResponseEntity.ok(uBody));
        when(orderRepo.findAllByUserNameAndProductName(TEST_USER, TEST_PRODUCT))
                .thenReturn(Collections.emptyList());

        ResponseEntity<UserProductsResponse<OrderDetails>> response = orderDetailsService.getOneOrderDetails(up);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("No Such Order is available", response.getBody().getMessage());
    }
//-------------------------------------
    //updatePaymentStatus
    @Test
    void updatePaymentStatus_WhenServiceReturnError_ShouldReturnBadRequest() {
        UserProducts up = new UserProducts(TEST_USER, TEST_PRODUCT);

        ResponseEntity<UserProductsResponse<UserProducts>> mockResponseEntity = mock(ResponseEntity.class);
        when(mockResponseEntity.getBody()).thenReturn(null);
        when(ecomUserService.validateUserProduct(any())).thenReturn(mockResponseEntity);

        ResponseEntity<UserProductsResponse<OrderDetails>> serviceResponse = orderDetailsService.updatePaymentStatus(up);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, serviceResponse.getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), serviceResponse.getBody().getStatus());
        assertNull(serviceResponse.getBody().getData());
        assertEquals("Something went wrong !", serviceResponse.getBody().getMessage());
    }
    @Test
    void updatePaymentStatus_WhenUserNotFound_ShouldReturnBadRequest() {
        UserProducts up = new UserProducts(TEST_USER, TEST_PRODUCT);
        UserProductsResponse<UserProducts> uBody = new UserProductsResponse<>();
        uBody.setStatus(400); // Other than 200
        when(ecomUserService.validateUserProduct(any())).thenReturn(ResponseEntity.ok(uBody));

        ResponseEntity<UserProductsResponse<OrderDetails>> response = orderDetailsService.updatePaymentStatus(up);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), Objects.requireNonNull(response.getBody()).getStatus());
        assertEquals("User doesn't exists", response.getBody().getMessage());
    }
    @Test
    void updatePaymentStatus_WhenOrderFoundAndPaymentStatusIsUnpaid_ShouldReturn200() {
        UserProducts up = new UserProducts(TEST_USER, TEST_PRODUCT);
        UserProductsResponse<UserProducts> uBody = new UserProductsResponse<>();
        uBody.setStatus(200);

        when(ecomUserService.validateUserProduct(any())).thenReturn(ResponseEntity.ok(uBody));
        when(orderRepo.findAllByUserNameAndProductName(TEST_USER, TEST_PRODUCT))
            .thenReturn(List.of(sampleOrder));

        ResponseEntity<UserProductsResponse<OrderDetails>> response = orderDetailsService.updatePaymentStatus(up);

        assertEquals("Paid", sampleOrder.getPaymentStatus());
        verify(orderRepo,times(1)).save(sampleOrder);
    }
    @Test
    void updatePaymentStatus_WhenOrderFoundAndPaymentStatusIsPaid_ShouldReturn200() {
        OrderDetails paidOrder = new OrderDetails(1, TEST_USER, TEST_PRODUCT, "Placed", "Paid");
        UserProducts up = new UserProducts(TEST_USER, TEST_PRODUCT);
        UserProductsResponse<UserProducts> uBody = new UserProductsResponse<>();
        uBody.setStatus(200);

        when(ecomUserService.validateUserProduct(any())).thenReturn(ResponseEntity.ok(uBody));
        when(orderRepo.findAllByUserNameAndProductName(TEST_USER, TEST_PRODUCT))
                .thenReturn(List.of(paidOrder));

        ResponseEntity<UserProductsResponse<OrderDetails>> response = orderDetailsService.updatePaymentStatus(up);

        assertEquals("Paid", paidOrder.getPaymentStatus());
        verify(orderRepo,never()).save(paidOrder);
    }
    @Test
    void updatePaymentStatus_WhenListIsEmpty_ShouldCompleteNormally() {
        UserProductsResponse<UserProducts> uBody = new UserProductsResponse<>();
        uBody.setStatus(200);

        when(ecomUserService.validateUserProduct(any())).thenReturn(ResponseEntity.ok(uBody));
        when(orderRepo.findAllByUserNameAndProductName(TEST_USER,TEST_PRODUCT)).thenReturn(Collections.emptyList());

        ResponseEntity<UserProductsResponse<OrderDetails>> response =
                orderDetailsService.updatePaymentStatus(new UserProducts(TEST_USER, TEST_PRODUCT));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(orderRepo, never()).save(any());
    }
//-------------------------------------
    //updateOrderStatus
    @Test
    void updateOrderStatus_WhenShipped_ShouldSetToDelivered() {
        sampleOrder.setOrderStatus("Shipped");
        sampleOrder.setPaymentStatus("Paid");
        when(orderRepo.findById(101)).thenReturn(Optional.of(sampleOrder));

        ResponseEntity<UserProductsResponse<OrderDetails>> response = orderDetailsService.updateOrderStatus(101);

        assertEquals("Delivered", Objects.requireNonNull(response.getBody()).getData().getOrderStatus());
        verify(orderRepo).save(sampleOrder);
}
    @Test
    void updateOrderStatus_WhenDelivered_ShouldDeleteAndSendKafka() {
        sampleOrder.setOrderStatus("Delivered");
        when(orderRepo.findById(101)).thenReturn(Optional.of(sampleOrder));

        ResponseEntity<UserProductsResponse<OrderDetails>> response = orderDetailsService.updateOrderStatus(101);

        verify(orderRepo).delete(sampleOrder);
        verify(userProductKafkaTemplate).send(eq("ecomOrderService_removeUserProduct"), any());
    }
    @Test
    void updateOrderStatus_WhenPlacedAndPaid_ShouldSetToShipped() {
        sampleOrder.setOrderStatus("Placed");
        sampleOrder.setPaymentStatus("Paid");
        when(orderRepo.findById(101)).thenReturn(Optional.of(sampleOrder));

        ResponseEntity<UserProductsResponse<OrderDetails>> response = orderDetailsService.updateOrderStatus(101);

        assertEquals("Shipped", response.getBody().getData().getOrderStatus());
        verify(orderRepo).save(sampleOrder);
    }
    @Test
    void updateOrderStatus_WhenPlacedButUnpaid_ShouldReturn402() {
        sampleOrder.setOrderStatus("Placed");
        sampleOrder.setPaymentStatus("Unpaid");
        when(orderRepo.findById(101)).thenReturn(Optional.of(sampleOrder));

        ResponseEntity<UserProductsResponse<OrderDetails>> response = orderDetailsService.updateOrderStatus(101);

        assertEquals(402, response.getBody().getStatus());
        assertEquals("Payment is due !", response.getBody().getMessage());
    }
//-------------------------------------
}