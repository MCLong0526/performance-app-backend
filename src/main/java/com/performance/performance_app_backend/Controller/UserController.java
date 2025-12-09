package com.performance.performance_app_backend.Controller;

import com.performance.performance_app_backend.Entity.User;
import com.performance.performance_app_backend.Service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map; // Import this

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    // GET all users - UPDATED HERE
    @GetMapping
    public Map<String, List<User>> getAll() {
// Import java.util.Map;
        Map<String, List<User>> response = new HashMap<>();
        response.put("data", service.getAll());
        return response;
    }

    // GET a user by ID
    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        return service.getById(id);
    }

    // CREATE new user
    @PostMapping
    public User create(@RequestBody User user) {
        return service.create(user);
    }

    // 👇 NEW: EXCEPTION HANDLER METHOD
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // Sets HTTP Status to 400
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        // Set the error key to the exception message
        errorResponse.put("error", ex.getMessage());

        // Return a ResponseEntity with the error body and 400 status
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    // UPDATE user
    @PutMapping("/{id}")
    public User update(@PathVariable Long id, @RequestBody User user) {
        return service.update(id, user);
    }

    // DELETE user
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        boolean deleted = service.delete(id);
        return deleted ? "User deleted" : "User not found";
    }
}