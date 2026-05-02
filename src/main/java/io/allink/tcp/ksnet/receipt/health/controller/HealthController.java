package io.allink.tcp.ksnet.receipt.health.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
  @GetMapping(path = "/health/ready")
  ResponseEntity<?> ready() {
    return ResponseEntity.ok("Ready");
  }
}
