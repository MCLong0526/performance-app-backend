package com.performance.performance_app_backend.Controller;

import com.performance.performance_app_backend.Entity.AnnualLeave;
import com.performance.performance_app_backend.Service.AnnualLeaveService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leave")
public class AnnualLeaveController {

    private final AnnualLeaveService service;

    public AnnualLeaveController(AnnualLeaveService service) {
        this.service = service;
    }

    // Helper method to create a standardized response (copied from UserController)
    private ResponseEntity<Map<String, Object>> createResponse(Object data, String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", status.value());
        response.put("msg", message);
        response.put("data", data);
        return new ResponseEntity<>(response, status);
    }

    private ResponseEntity<Map<String, Object>> createErrorResponse(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", status.value());
        response.put("msg", message);
        response.put("data", null);
        return new ResponseEntity<>(response, status);
    }

    // 1. GET all leave requests (Admin/HR View)
    @GetMapping("/all")
    // NOTE: In a real app, you would add @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('HR')")
    public ResponseEntity<Map<String, Object>> getAllLeave() {
        List<AnnualLeave> leaves = service.getAll();
        return createResponse(leaves, "All leave requests fetched successfully.", HttpStatus.OK);
    }

    // 2. GET leave requests for a specific user
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getLeaveByUserId(@PathVariable Long userId) {
        // NOTE: Add security check to ensure requesting user is either the {userId} or an ADMIN/MANAGER
        List<AnnualLeave> leaves = service.getByUserId(userId);
        return createResponse(leaves, "Leave requests for user fetched successfully.", HttpStatus.OK);
    }

    // 3. GET a single leave request by ID
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getLeaveById(@PathVariable Long id) {
        AnnualLeave leave = service.getById(id);
        if (leave != null) {
            return createResponse(leave, "Leave request fetched successfully.", HttpStatus.OK);
        }
        return createErrorResponse("Leave request not found or deleted.", HttpStatus.NOT_FOUND);
    }

    // 4. CREATE new leave request (Employee submission)
    @PostMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> createLeave(@PathVariable Long userId, @RequestBody AnnualLeave leave) {
        try {
            AnnualLeave createdLeave = service.create(leave, userId);
            return createResponse(createdLeave, "Leave request submitted successfully (PENDING).", HttpStatus.CREATED);
        } catch (IllegalArgumentException ex) {
            // Catches validation errors like "Insufficient annual leave"
            return createErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // 5. APPROVE leave request (Manager/Admin action)
    @PutMapping("/{id}/approve")
    // NOTE: Add @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<Map<String, Object>> approveLeave(@PathVariable Long id) {
        AnnualLeave approvedLeave = service.approveLeave(id);
        if (approvedLeave != null) {
            return createResponse(approvedLeave, "Leave request approved. User's leave days updated.", HttpStatus.OK);
        }
        return createErrorResponse("Leave request not found, deleted, or already processed.", HttpStatus.NOT_FOUND);
    }

    // 6. REJECT leave request (Manager/Admin action)
    @PutMapping("/{id}/reject")
    // NOTE: Add @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<Map<String, Object>> rejectLeave(@PathVariable Long id) {
        AnnualLeave rejectedLeave = service.rejectLeave(id);
        if (rejectedLeave != null) {
            return createResponse(rejectedLeave, "Leave request rejected.", HttpStatus.OK);
        }
        return createErrorResponse("Leave request not found, deleted, or already processed.", HttpStatus.NOT_FOUND);
    }

    // 7. DELETE (Cancel) leave request (Employee/Admin action)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteLeave(@PathVariable Long id) {
        boolean deleted = service.delete(id);
        if (deleted) {
            return createResponse(null, "Leave request soft-deleted/cancelled (days refunded if approved).", HttpStatus.OK);
        }
        return createErrorResponse("Leave request not found or already deleted.", HttpStatus.NOT_FOUND);
    }

    // 8. EXCEPTION HANDLER (HTTP 400 Validation Error)
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("code", String.valueOf(HttpStatus.BAD_REQUEST.value()));
        errorResponse.put("msg", "Validation Error");
        errorResponse.put("error", ex.getMessage()); // Detailed validation message

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateLeave(
            @PathVariable Long id,
            @RequestBody AnnualLeave updatedLeave
    ) {
        try {
            AnnualLeave leave = service.update(id, updatedLeave);
            return createResponse(leave, "Leave request updated successfully.", HttpStatus.OK);
        } catch (IllegalArgumentException ex) {
            return createErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}