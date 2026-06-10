package com.ticket_test_app.keycloak_demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/demo")
public class DemoController {

    @GetMapping
    public String hello(){
        return "Hello World";
    }

    @GetMapping("/hello-2")
    public String hello2(){
        return "Hello World2 - ADMIN";
    }

}
