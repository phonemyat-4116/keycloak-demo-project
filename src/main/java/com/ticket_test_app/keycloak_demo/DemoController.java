package com.ticket_test_app.keycloak_demo;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('CLIENT_ADMIN')")
    public String hello2(){
        return "Hello World2 - ADMIN";
    }

    @GetMapping("/hello-3")
    @PreAuthorize("hasRole('CLIENT_USER')")
    public String hello3(){
        return "Hello World3 - USER";
    }

}
