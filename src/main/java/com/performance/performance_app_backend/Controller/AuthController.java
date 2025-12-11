package com.performance.performance_app_backend.Controller;

import com.performance.performance_app_backend.Config.Jwt.JwtService; // 🛑 NEW IMPORT
import com.performance.performance_app_backend.Entity.User;
import com.performance.performance_app_backend.Service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

// DTO for simplified login request (only needs email and password)
class LoginRequest {
    public String email;
    public String password;
}

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService; // 🛑 NEW FIELD

    // 🛑 MODIFIED CONSTRUCTOR: Inject JwtService
    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        User user = userService.authenticate(request.email, request.password);

        if (user != null) {

            // 🛑 NEW: 1. Generate the JWT Token upon successful authentication
            String token = jwtService.generateToken(user.getEmail());

            Map<String, Object> response = new HashMap<>();
            response.put("code", HttpStatus.OK.value());
            response.put("msg", "Login successful.");

            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("email", user.getEmail());
            userData.put("name", user.getName());
            userData.put("role", user.getRole());

            // 🛑 NEW: 2. Include the generated token in the response data
            userData.put("token", token);

            response.put("data", userData);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            // Authentication failed
            Map<String, Object> response = new HashMap<>();
            response.put("code", HttpStatus.UNAUTHORIZED.value());
            response.put("msg", "Invalid credentials.");
            response.put("data", null);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }
}