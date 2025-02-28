package com.hackathon.blockchain.controller;

import com.hackathon.blockchain.model.User;
import com.hackathon.blockchain.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody User user,
            HttpSession session
    ) {
        try {
            User registeredUser = userService.register(
                    user.getUsername(),
                    user.getEmail(),
                    user.getPassword()
            );

            // Crear sesión
            session.setAttribute("user", registeredUser);

            return ResponseEntity.ok().body(
                    createResponse("User registered and logged in successfully")
            );

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    createResponse("Registration failed: " + e.getMessage())
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request,
            HttpSession session
    ) {
        try {
            User user = userService.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!userService.validatePassword(request.getPassword(), user.getPassword())) {
                throw new RuntimeException("Invalid credentials");
            }

            session.setAttribute("user", user);

            return ResponseEntity.ok().body(
                    createResponse("Login successful")
            );

        } catch (Exception e) {
            return ResponseEntity.status(401).body(
                    createResponse("❌ Invalid credentials")
            );
        }
    }

    @GetMapping("/check-session")
    public ResponseEntity<?> checkSession(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            Map<String, Object> response = new HashMap<>();
            response.put("user", authentication.getPrincipal());
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(401).build();
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok().body(
                createResponse("Logged out successfully")
        );
    }

    private Map<String, Object> createResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        return response;
    }

    // Clase interna para el request de login
    static class LoginRequest {
        private String username;
        private String password;

        // Getters y Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}