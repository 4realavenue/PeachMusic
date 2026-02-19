package com.example.peachmusic.common.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AdminPingController {

    @GetMapping("/admin/ping")
    public ResponseEntity<Void> ping() {
        return ResponseEntity.ok().build();
    }
}
