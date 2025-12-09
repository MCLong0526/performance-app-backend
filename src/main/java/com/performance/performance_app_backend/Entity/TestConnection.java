package com.performance.performance_app_backend.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "test_connection")
@Data
public class TestConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;
}


