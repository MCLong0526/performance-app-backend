package com.performance.performance_app_backend.Service;

import com.performance.performance_app_backend.Entity.User;
import com.performance.performance_app_backend.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public List<User> getAll() {
        return repo.findAll();
    }

    public User getById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public User create(User user) {
        if(user.getId() != null && repo.existsById(user.getId())) {
            throw new IllegalArgumentException("User with ID " + user.getId() + " already exists.");
        }

        // 👇 UPDATED CHECK: Check for null OR empty/blank strings
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty.");
        }

        if(user.getRole() == null) {
            user.setRole("USER");
        }

        return repo.save(user);
    }

    public User update(Long id, User newData) {
        return repo.findById(id)
                .map(u -> {
                    u.setName(newData.getName());
                    u.setEmail(newData.getEmail());
                    u.setRole(newData.getRole());
                    return repo.save(u);
                })
                .orElse(null);
    }

    public boolean delete(Long id) {
        if (repo.existsById(id)) {
            repo.deleteById(id);
            return true;
        }
        return false;
    }
}
