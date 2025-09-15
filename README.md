# Ecommerce_Microservices

This project is the **Microservices version** of my Ecommerce backend system.
It started as a **Monolithic Spring Boot application** ([Ecommerce_Monolithic](../Ecommerce_Monolithic)) and was later **refactored into microservices** for scalability and modularity.

---

## 🛠 Tech Stack

- **Backend Framework:** Spring Boot
- **Service Discovery:** Eureka Server
- **Inter-Service Communication:** OpenFeign
- **Database:** MySQL (with JPA/Hibernate, auto table creation enabled)
- **Build Tool:** Maven

---

## 📌 Microservices in this Project

1.  **User Service (ECOMUSERS)**
    - User registration, login, and profile management
    - Stores and fetches user details
    - Manages user’s product interactions

2.  **Product Service (ECOMPRODUCTS)**
    - Add new products, update stock
    - Search and fetch product details

3.  **Order Service (ECOMORDERSERVICE)**
    - Place orders, cancel orders, update status
    - Fetch order details for users
    - Calls **User Service** and **Product Service** using Feign Clients

4.  **Payment Service (dummy for now)**
    - Simulates marking an order as paid
    - Maintains transaction logs

5.  **Eureka Server**
    - Service registry for dynamic discovery
    - All services (Users, Products, Orders, Payments) register themselves here

---

## ⚡ Features

- **Service Discovery with Eureka** → No hardcoding service URLs
- **Inter-Service Communication via Feign** → Cleaner API calls between services
- **Scalable Structure** → Each service can be deployed, scaled, and maintained independently
- **Database per Service** → Following microservices best practices (loosely coupled services)

---

## 🚀 How to Run

1.  Start **Eureka Server** first:
    ```bash
    cd eureka-server
    mvn spring-boot:run
    ```

2.  Start individual services:
    ```bash
    cd ecomUserService
    mvn spring-boot:run

    cd ecomProductService
    mvn spring-boot:run

    cd ecomOrderService
    mvn spring-boot:run

    cd ecomPaymentService
    mvn spring-boot:run
    ```

3.  Access Eureka Dashboard:
    - **URL:** `http://localhost:8761`
    - You should see all services registered there.

4.  Example API Workflows
    ```bash
    # Register User:
    POST /users/register

    # Add Product:
    POST /product/add

    # Place Order:
    POST /orders/placeOrder/{userId}

    # Get User Orders:
    GET /orders/getAllOrderDetails/{userName}
    (This internally calls User + Product services via Feign)
    ```

---

## 📁 Project Structure

```bash
Ecommerce_Microservices/
├── eureka-server/
├── ecomUserService/
├── ecomProductService/
├── ecomOrderService/
├── ecomPaymentService/
└── README.md
```
---
## 🏆 Credits
Developed by Prathamesh Chandekar as part of learning Spring Boot Microservices Architecture.
