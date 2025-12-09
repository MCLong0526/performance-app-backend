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
