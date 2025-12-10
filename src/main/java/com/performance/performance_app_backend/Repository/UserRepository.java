package com.performance.performance_app_backend.Repository;

import com.performance.performance_app_backend.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByIsDeletedFalse();
}