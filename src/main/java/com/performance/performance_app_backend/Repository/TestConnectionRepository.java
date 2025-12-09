package com.performance.performance_app_backend.Repository;

import com.performance.performance_app_backend.Entity.TestConnection;
import org.springframework.data.jpa.repository.JpaRepository;


public interface TestConnectionRepository extends JpaRepository<TestConnection, Long> {
}