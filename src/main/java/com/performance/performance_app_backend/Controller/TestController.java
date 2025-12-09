package com.performance.performance_app_backend.Controller;

import com.performance.performance_app_backend.Entity.TestConnection;
import com.performance.performance_app_backend.Repository.TestConnectionRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TestController {

    private final TestConnectionRepository repo;

    public TestController(TestConnectionRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/test-db")
    public List<TestConnection> testDb() {
        return repo.findAll();
    }
}
