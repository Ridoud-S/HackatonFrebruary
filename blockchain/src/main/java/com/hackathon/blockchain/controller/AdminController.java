package com.hackathon.blockchain.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @GetMapping("/dashboard")
    public ResponseEntity<?> adminDashboard() {
        return ResponseEntity.ok().body(
                Map.of("message", "Welcome to Admin Dashboard")
        );
    }

    @GetMapping("/users")
    public ResponseEntity<?> listAllUsers() {
        // Lógica para listar usuarios
        return ResponseEntity.ok().body(
                Map.of("users", "List of all users")
        );
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        // Lógica para eliminar usuario
        return ResponseEntity.ok().body(
                Map.of("message", "User " + id + " deleted")
        );
    }
}