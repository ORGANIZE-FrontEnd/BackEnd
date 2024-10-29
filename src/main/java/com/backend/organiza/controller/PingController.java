package com.backend.organiza.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ping")
public class PingController {

    @Operation(summary = "Ping to prevent BED from sleeping")
    @GetMapping()
    public ResponseEntity<String> pingServer() {
        return ResponseEntity.status(HttpStatus.OK).body("Pong!");
    }
}
