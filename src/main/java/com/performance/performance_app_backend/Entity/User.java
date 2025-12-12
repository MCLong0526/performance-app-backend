package com.performance.performance_app_backend.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.performance.performance_app_backend.Enum.Role;
import com.performance.performance_app_backend.Enum.UserStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
// 🛑 NEW IMPORTS FOR SPRING SECURITY
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
// 🛑 CRITICAL FIX: IMPLEMENT USERDETAILS
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    // Use Role Enum for role field
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // Stores the user's role (e.g., PROGRAMMER, ADMIN)

    // Use UserStatus Enum for status field
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(unique = true, length = 15)
    private String phoneNumber;

    // --- Audit Fields ---
    @Column(nullable = false)
    private String createdBy;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createTime;

    private String updatedBy;

    @UpdateTimestamp
    private LocalDateTime updateTime;

    @Column(nullable = false)
    private boolean isDeleted = false;

    @Column(nullable = false)
    private int annualLeaveEntitlement = 14; // Default to 14 days

    @Column(nullable = false)
    private BigDecimal annualLeaveUsed = BigDecimal.ZERO; // Default to 0.00 days

    // NEW FIELD: Password - Use @JsonIgnore to hide it from serialized API responses
    // We keep @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) if it's used for incoming JSON
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    public User() {
        if (this.role == null) {
            this.role = Role.PROGRAMMER;
        }
        if (this.status == null) {
            this.status = UserStatus.ACTIVE;
        }
    }

    // Constructor (Retained)
    public User(String name, String email, Role role, String createdBy, String phoneNumber) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.createdBy = createdBy;
        this.status = UserStatus.ACTIVE;
        this.phoneNumber = phoneNumber;
    }

    // --- SPRING SECURITY USERDETAILS IMPLEMENTATION ---

    /**
     * Maps the user's Role Enum to a Spring Security GrantedAuthority.
     * Required for authorization checks (e.g., @PreAuthorize).
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // NOTE: The role must be prefixed with "ROLE_" if you use hasRole()
        // Here, we're returning the raw role name from the Enum.
        return java.util.Collections.singletonList(new SimpleGrantedAuthority(role.name()));
    }

    /**
     * CRITICAL: Returns the field used for authentication lookup (email).
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Returns the hashed password.
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Account status check methods. Base implementation assumes all are true.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Can be tied to UserStatus or a separate lock field
        return this.status != UserStatus.LOCKED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Tied to soft delete or general status
        return !isDeleted && this.status == UserStatus.ACTIVE;
    }

    // --- GETTERS & SETTERS (Existing Methods) ---
    // Note: Password getters/setters are implicitly used by Spring Security

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    // Note: The getter/setter for password should stay, but Spring Security uses the @Override getPassword()
    public void setPassword(String password) {
        this.password = password;
    }

    public int getAnnualLeaveEntitlement() {
        return annualLeaveEntitlement;
    }

    public void setAnnualLeaveEntitlement(int annualLeaveEntitlement) {
        this.annualLeaveEntitlement = annualLeaveEntitlement;
    }

    public BigDecimal getAnnualLeaveUsed() {
        return annualLeaveUsed;
    }

    public void setAnnualLeaveUsed(BigDecimal annualLeaveUsed) {
        this.annualLeaveUsed = annualLeaveUsed;
    }
}