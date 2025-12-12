package com.performance.performance_app_backend.Repository;

import com.performance.performance_app_backend.Entity.AnnualLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnnualLeaveRepository extends JpaRepository<AnnualLeave, Long> {
    // Find all non-deleted leave records for a specific user
    List<AnnualLeave> findByUserIdAndIsDeletedFalse(Long userId);

    // Find all non-deleted leave records (for admin/reporting)
    List<AnnualLeave> findByIsDeletedFalse();
}