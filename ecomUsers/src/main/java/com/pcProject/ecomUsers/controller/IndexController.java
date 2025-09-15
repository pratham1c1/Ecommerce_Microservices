package com.pcProject.ecomUsers.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {

    @GetMapping("/")
    public String IndexPage(){
        return "Welcome to Ecommerce website";
    }
}