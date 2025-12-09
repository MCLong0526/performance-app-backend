package com.performance.performance_app_backend.Controller;

import com.performance.performance_app_backend.Entity.User;
import com.performance.performance_app_backend.Service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*") // allow frontend to call it
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    // GET all users
    @GetMapping
    public List<User> getAll() {
        return service.getAll();
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
