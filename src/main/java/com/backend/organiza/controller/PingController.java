package com.backend.organiza.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class PingController {

    @Operation(summary = "Ping to prevent BED from sleeping")
    @GetMapping("api/ping")
    public ResponseEntity<String> pingServer() {
        return ResponseEntity.status(HttpStatus.OK).body("Pong!");
    }


    @RequestMapping(value = "api/ping", method = RequestMethod.HEAD)
    @ResponseStatus(HttpStatus.OK)
    public void handleHeadPing() {
    }
}
