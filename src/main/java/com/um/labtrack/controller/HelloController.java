package com.um.labtrack.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Sample REST Controller to demonstrate backend API functionality.
 * This controller provides a simple endpoint that returns a JSON message,
 * which can be consumed by the Swing UI frontend.
 */
@RestController
@RequestMapping("/api")
public class HelloController {

    /**
     * Simple GET endpoint that returns a greeting message in JSON format.
     * This endpoint is used to test the connection between the Swing UI and Spring Boot backend.
     *
     * @return ResponseEntity containing a JSON message
     */
    @GetMapping("/hello")
    public ResponseEntity<Map<String, String>> hello() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hello from LabTrack UM Backend!");
        response.put("status", "success");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(response);
    }
}
