package com.flyover.example;

import java.util.Collections;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Main Spring Boot application configuration class. 
 * 
 * @author mramach
 *
 */
@SpringBootApplication
@RestController
public class Application {
    
    @RequestMapping(value = "say-hello")
    public ResponseEntity<Map<String, Object>> sayHello(
            @RequestParam(value = "name", defaultValue = "world") String name) {
        
        return ResponseEntity.ok(Collections.singletonMap("hello", name));
        
    }
    
    /**
     * Root entry point for Spring Boot application.
     * 
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
}
