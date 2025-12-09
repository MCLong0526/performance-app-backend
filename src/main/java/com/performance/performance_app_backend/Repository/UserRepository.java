package com.performance.performance_app_backend.Repository;

import com.performance.performance_app_backend.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
