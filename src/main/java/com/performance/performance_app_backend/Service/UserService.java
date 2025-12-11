package com.performance.performance_app_backend.Service;

import com.performance.performance_app_backend.Entity.User;
import com.performance.performance_app_backend.Enum.Role;
import com.performance.performance_app_backend.Enum.UserStatus;
import com.performance.performance_app_backend.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository repo;
    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public User authenticate(String email, String rawPassword) {
        Optional<User> userOpt = repo.findByEmailAndIsDeletedFalse(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(rawPassword, user.getPassword())) {
                return user;
            }
        }
        return null;
    }

    public Optional<User> findByEmail(String email) {
        return repo.findByEmailAndIsDeletedFalse(email);
    }

    public List<User> getAll() {
        return repo.findByIsDeletedFalse();
    }

    public User getById(Long id) {
        return repo.findById(id)
                .filter(user -> !user.isDeleted())
                .orElse(null);
    }

    public User create(User user) {
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty.");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if(user.getRole() == null) {
            user.setRole(Role.PROGRAMMER);
        }
        if(user.getStatus() == null) {
            user.setStatus(UserStatus.ACTIVE);
        }
        if(user.getCreatedBy() == null || user.getCreatedBy().trim().isEmpty()) {
            user.setCreatedBy("SYSTEM_OR_ADMIN");
        }
        user.setCreateTime(LocalDateTime.now());
        user.setDeleted(false);
        return repo.save(user);
    }

    @Transactional
    public User update(Long id, User newData) {
        return repo.findById(id)
                .filter(u -> !u.isDeleted())
                .map(u -> {
                    u.setName(newData.getName() != null ? newData.getName() : u.getName());
                    u.setEmail(newData.getEmail() != null ? newData.getEmail() : u.getEmail());
                    u.setRole(newData.getRole() != null ? newData.getRole() : u.getRole());
                    u.setStatus(newData.getStatus() != null ? newData.getStatus() : u.getStatus());
                    u.setPhoneNumber(newData.getPhoneNumber() != null ? newData.getPhoneNumber() : u.getPhoneNumber());
                    if (newData.getPassword() != null && !newData.getPassword().trim().isEmpty()) {
                        u.setPassword(passwordEncoder.encode(newData.getPassword()));
                    }
                    u.setUpdatedBy("SYSTEM_OR_ADMIN_UPDATER");
                    u.setUpdateTime(LocalDateTime.now());
                    return repo.save(u);
                })
                .orElse(null);
    }

    @Transactional
    public boolean delete(Long id) {
        Optional<User> userOpt = repo.findById(id);
        if (userOpt.isPresent() && !userOpt.get().isDeleted()) {
            User user = userOpt.get();
            user.setDeleted(true);
            user.setUpdatedBy("SYSTEM_OR_ADMIN_DELETER");
            user.setUpdateTime(LocalDateTime.now());
            repo.save(user);
            return true;
        }
        return false;
    }
}
