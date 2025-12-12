package com.performance.performance_app_backend.Service;

import com.performance.performance_app_backend.Entity.AnnualLeave;
import com.performance.performance_app_backend.Entity.User;
import com.performance.performance_app_backend.Enum.LeaveStatus;
import com.performance.performance_app_backend.Enum.LeaveType;
import com.performance.performance_app_backend.Repository.AnnualLeaveRepository;
import com.performance.performance_app_backend.Repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.performance.performance_app_backend.Enum.LeaveStatus;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AnnualLeaveService {

    private final AnnualLeaveRepository leaveRepo;
    private final UserRepository userRepo; // Needed to update User's used leave days

    public AnnualLeaveService(AnnualLeaveRepository leaveRepo, UserRepository userRepo) {
        this.leaveRepo = leaveRepo;
        this.userRepo = userRepo;
    }

    // Helper to get current authenticated user's name (copied from UserService)
    private String getCurrentUserName() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return ((User) authentication.getPrincipal()).getName();
        }
        return "SYSTEM_OR_ADMIN";
    }

    /**
     * Finds a single, non-deleted annual leave record by ID.
     */
    public AnnualLeave getById(Long id) {
        return leaveRepo.findById(id)
                .filter(leave -> !leave.isDeleted())
                .orElse(null);
    }

    /**
     * Finds all non-deleted leave records for a specific user.
     */
    public List<AnnualLeave> getByUserId(Long userId) {
        return leaveRepo.findByUserIdAndIsDeletedFalse(userId);
    }

    /**
     * Finds all non-deleted leave records (Admin/HR view).
     */
    public List<AnnualLeave> getAll() {
        return leaveRepo.findByIsDeletedFalse();
    }

    /**
     * Submits a new annual leave request.
     */
    @Transactional
    public AnnualLeave create(AnnualLeave leave, Long userId) {
        // 1. Basic Validation
        if (leave.getDuration() == null || leave.getDuration().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Leave duration must be greater than 0.");
        }

        // 2. Fetch User
        User user = userRepo.findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("User not found or is deleted."));

        // 3. Check for sufficient leave entitlement
        BigDecimal remainingLeave = BigDecimal.valueOf(user.getAnnualLeaveEntitlement()).subtract(user.getAnnualLeaveUsed());
        if (leave.getDuration().compareTo(remainingLeave) > 0) {
            throw new IllegalArgumentException("Insufficient annual leave remaining. Available: " + remainingLeave);
        }

        // 4. Set required fields and Audit
        leave.setUser(user);
        leave.setStatus(LeaveStatus.PENDING); // Always PENDING on creation
        leave.setCreatedBy(getCurrentUserName());
        leave.setCreateTime(LocalDateTime.now());
        leave.setType(
                leave.getType() != null ? leave.getType() : LeaveType.ANNUAL
        );

        leave.setDeleted(false);

        // 5. Save the request
        return leaveRepo.save(leave);
    }

    /**
     * Approves a leave request and updates the user's used leave days.
     * Only callable by Admin/Manager roles (you'd add security checks in the Controller).
     */
    @Transactional
    public AnnualLeave approveLeave(Long leaveId) {
        return leaveRepo.findById(leaveId)
                .filter(l -> !l.isDeleted() && l.getStatus() == LeaveStatus.PENDING)
                .map(leave -> {
                    // Update leave status
                    leave.setStatus(LeaveStatus.APPROVED);

                    // Update audit
                    leave.setUpdatedBy(getCurrentUserName());
                    leave.setUpdateTime(LocalDateTime.now());

                    // CRITICAL: Update User's used leave days
                    User user = leave.getUser();
                    BigDecimal newUsed = user.getAnnualLeaveUsed().add(leave.getDuration());
                    user.setAnnualLeaveUsed(newUsed);
                    userRepo.save(user); // Save updated user

                    return leaveRepo.save(leave); // Save updated leave
                })
                .orElse(null);
    }

    /**
     * Rejects a leave request.
     */
    @Transactional
    public AnnualLeave rejectLeave(Long leaveId) {
        return leaveRepo.findById(leaveId)
                .filter(l -> !l.isDeleted() && l.getStatus() == LeaveStatus.PENDING)
                .map(leave -> {
                    leave.setStatus(LeaveStatus.REJECTED);
                    leave.setUpdatedBy(getCurrentUserName());
                    leave.setUpdateTime(LocalDateTime.now());
                    return leaveRepo.save(leave);
                })
                .orElse(null);
    }

    /**
     * Cancel/Soft-delete a leave record. If APPROVED, it must also refund the days.
     */
    @Transactional
    public boolean delete(Long id) {
        Optional<AnnualLeave> leaveOpt = leaveRepo.findById(id);

        if (leaveOpt.isPresent() && !leaveOpt.get().isDeleted()) {
            AnnualLeave leave = leaveOpt.get();

            // If the leave was APPROVED, we must "refund" the days to the user
            if (leave.getStatus() == LeaveStatus.APPROVED) {
                User user = leave.getUser();
                BigDecimal newUsed = user.getAnnualLeaveUsed().subtract(leave.getDuration());
                user.setAnnualLeaveUsed(newUsed);
                userRepo.save(user); // Save updated user
            }

            leave.setDeleted(true);
            leave.setStatus(LeaveStatus.CANCELED); // Set status to CANCELED/DELETED
            leave.setUpdatedBy(getCurrentUserName());
            leave.setUpdateTime(LocalDateTime.now());
            leaveRepo.save(leave);
            return true;
        }
        return false;
    }

    @Transactional
    public AnnualLeave update(Long id, AnnualLeave updated) {
        AnnualLeave existing = leaveRepo.findById(id)
                .filter(l -> !l.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found."));

        // ❗ Only allow edit when PENDING
        if (existing.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalArgumentException("Only PENDING leave requests can be edited.");
        }

        existing.setType(updated.getType());
        existing.setReason(updated.getReason());
        existing.setStartDate(updated.getStartDate());
        existing.setEndDate(updated.getEndDate());
        existing.setDuration(updated.getDuration());

        existing.setUpdatedBy(getCurrentUserName());
        existing.setUpdateTime(LocalDateTime.now());

        return leaveRepo.save(existing);
    }

}