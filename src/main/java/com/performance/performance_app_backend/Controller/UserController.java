package com.performance.performance_app_backend.Controller;

import com.performance.performance_app_backend.Entity.User;
import com.performance.performance_app_backend.Service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
// Removed @CrossOrigin to rely on the global CorsConfig
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    // Helper method to create a standardized success response
    private ResponseEntity<Map<String, Object>> createSuccessResponse(Object data, String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", status.value());
        response.put("msg", message);
        response.put("data", data);
        return new ResponseEntity<>(response, status);
    }

    // Helper method to create a standardized error response (for not found/deletion edge cases)
    private ResponseEntity<Map<String, Object>> createErrorResponse(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", status.value());
        response.put("msg", message);
        response.put("data", null);
        return new ResponseEntity<>(response, status);
    }


    // 1. GET all users (HTTP 200)
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAll() {
        List<User> users = service.getAll();
        return createSuccessResponse(users, "Users fetched successfully.", HttpStatus.OK);
    }

    // 2. GET a user by ID (HTTP 200 or 404)
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        User user = service.getById(id);
        if (user != null) {
            return createSuccessResponse(user, "User fetched successfully.", HttpStatus.OK);
        }
        return createErrorResponse("User not found or already deleted.", HttpStatus.NOT_FOUND);
    }

    // 3. CREATE new user (HTTP 201 Created)
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody User user) {
        User createdUser = service.create(user);
        return createSuccessResponse(createdUser, "User created successfully.", HttpStatus.CREATED);
    }

    // 4. UPDATE user (HTTP 200 or 404)
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody User user) {
        User updatedUser = service.update(id, user);
        if (updatedUser != null) {
            return createSuccessResponse(updatedUser, "User updated successfully.", HttpStatus.OK);
        }
        return createErrorResponse("User not found or update failed.", HttpStatus.NOT_FOUND);
    }

    // 5. DELETE user (Soft Delete) (HTTP 200 or 404)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        boolean deleted = service.delete(id);
        if (deleted) {
            return createSuccessResponse(null, "User soft-deleted successfully.", HttpStatus.OK);
        }
        return createErrorResponse("User not found or already soft-deleted.", HttpStatus.NOT_FOUND);
    }

    // 6. EXCEPTION HANDLER (HTTP 400 Validation Error)
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("code", String.valueOf(HttpStatus.BAD_REQUEST.value()));
        errorResponse.put("msg", "Validation Error");
        errorResponse.put("error", ex.getMessage()); // Detailed validation message

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}