package com.performance.performance_app_backend.Entity;

import com.performance.performance_app_backend.Enum.Role;
import com.performance.performance_app_backend.Enum.UserStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    // Use Role Enum for role field
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Use UserStatus Enum for status field
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status; // New field

    @Column(unique = true, length = 15) // Assuming a max length for phone number
    private String phoneNumber; // New field

    // --- Audit Fields ---
    @Column(nullable = false)
    private String createdBy; // New field - The user who created this record

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createTime; // New field

    private String updatedBy; // New field - The last user who updated this record

    @UpdateTimestamp
    private LocalDateTime updateTime; // New field

    @Column(nullable = false)
    private boolean isDeleted = false; // New field - For soft delete

    public User() {
        // Default role and status upon creation if not explicitly set
        if (this.role == null) {
            this.role = Role.PROGRAMMER; // A sensible default role
        }
        if (this.status == null) {
            this.status = UserStatus.ACTIVE; // Default to ACTIVE
        }
    }

    // Constructor (You may want to adjust this based on your needs)
    public User(String name, String email, Role role, String createdBy, String phoneNumber) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.createdBy = createdBy;
        this.status = UserStatus.ACTIVE;
        this.phoneNumber = phoneNumber;
    }

    // Getters & Setters for all fields
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    // Role (Now using Enum)
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    // Status (Now using Enum)
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    // Phone Number
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    // Created By
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    // Create Time
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    // Updated By
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    // Update Time
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    // Is Deleted
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }
}