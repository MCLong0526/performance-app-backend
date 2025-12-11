package com.performance.performance_app_backend.Repository;

import com.performance.performance_app_backend.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByIsDeletedFalse();
    Optional<User> findByEmailAndIsDeletedFalse(String email);
}