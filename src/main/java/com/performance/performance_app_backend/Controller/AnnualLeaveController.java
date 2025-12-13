package com.performance.performance_app_backend.Controller;

import com.performance.performance_app_backend.Entity.AnnualLeave;
import com.performance.performance_app_backend.Entity.User;
import com.performance.performance_app_backend.Enum.Role;
import com.performance.performance_app_backend.Service.AnnualLeaveService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    // Helper method to create a standardized response
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

    // 1. GET all leave requests (BOSS/ADMIN View)
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllLeave() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        // Only BOSS or ADMIN can see all leave requests
        if (currentUser.getRole() == Role.BOSS || currentUser.getRole() == Role.ADMIN) {
            List<AnnualLeave> leaves = service.getAll();
            return createResponse(leaves, "All leave requests fetched successfully.", HttpStatus.OK);
        }
        // Programmers/Employees can only see their own leave requests
        else {
            List<AnnualLeave> leaves = service.getByUserId(currentUser.getId());
            return createResponse(leaves, "Your leave requests fetched successfully.", HttpStatus.OK);
        }
    }

    // 2. GET leave requests for a specific user
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getLeaveByUserId(@PathVariable Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        // If the user is not BOSS/ADMIN and is trying to access someone else's records, block them
        if (!currentUser.getId().equals(userId) && !(currentUser.getRole() == Role.BOSS || currentUser.getRole() == Role.ADMIN)) {
            return createErrorResponse("You are not authorized to view this user's leave requests.", HttpStatus.FORBIDDEN);
        }

        // If the user is accessing their own records or is a BOSS/ADMIN, allow access
        List<AnnualLeave> leaves = service.getByUserId(userId);
        return createResponse(leaves, "Leave requests fetched successfully.", HttpStatus.OK);
    }

    // 3. GET a single leave request by ID
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getLeaveById(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        AnnualLeave leave = service.getById(id);

        if (leave == null) {
            return createErrorResponse("Leave request not found or deleted.", HttpStatus.NOT_FOUND);
        }

        // If the user is not BOSS/ADMIN, they can only access their own leave request
        if (!leave.getUser().getId().equals(currentUser.getId()) && !(currentUser.getRole() == Role.BOSS || currentUser.getRole() == Role.ADMIN)) {
            return createErrorResponse("You are not authorized to view this leave request.", HttpStatus.FORBIDDEN);
        }

        return createResponse(leave, "Leave request fetched successfully.", HttpStatus.OK);
    }

    // 4. CREATE new leave request (Employee submission)
    @PostMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> createLeave(@PathVariable Long userId, @RequestBody AnnualLeave leave) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        // If the user is not BOSS/ADMIN, they can only create leave requests for themselves
        if (!currentUser.getId().equals(userId) && !(currentUser.getRole() == Role.BOSS || currentUser.getRole() == Role.ADMIN)) {
            return createErrorResponse("You are not authorized to create a leave request for this user.", HttpStatus.FORBIDDEN);
        }

        try {
            AnnualLeave createdLeave = service.create(leave, userId);
            return createResponse(createdLeave, "Leave request submitted successfully (PENDING).", HttpStatus.CREATED);
        } catch (IllegalArgumentException ex) {
            return createErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // 5. APPROVE leave request (BOSS/Admin action)
    @PutMapping("/{id}/approve")
    public ResponseEntity<Map<String, Object>> approveLeave(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        // Only BOSS/ADMIN can approve leave requests
        if (!(currentUser.getRole() == Role.BOSS || currentUser.getRole() == Role.ADMIN)) {
            return createErrorResponse("You are not authorized to approve leave requests.", HttpStatus.FORBIDDEN);
        }

        AnnualLeave approvedLeave = service.approveLeave(id);
        if (approvedLeave != null) {
            return createResponse(approvedLeave, "Leave request approved. User's leave days updated.", HttpStatus.OK);
        }
        return createErrorResponse("Leave request not found, deleted, or already processed.", HttpStatus.NOT_FOUND);
    }

    // 6. REJECT leave request (BOSS/Admin action)
    @PutMapping("/{id}/reject")
    public ResponseEntity<Map<String, Object>> rejectLeave(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        // Only BOSS/ADMIN can reject leave requests
        if (!(currentUser.getRole() == Role.BOSS || currentUser.getRole() == Role.ADMIN)) {
            return createErrorResponse("You are not authorized to reject leave requests.", HttpStatus.FORBIDDEN);
        }

        AnnualLeave rejectedLeave = service.rejectLeave(id);
        if (rejectedLeave != null) {
            return createResponse(rejectedLeave, "Leave request rejected.", HttpStatus.OK);
        }
        return createErrorResponse("Leave request not found, deleted, or already processed.", HttpStatus.NOT_FOUND);
    }

    // 7. DELETE (Cancel) leave request (Employee/BOSS/Admin action)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteLeave(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        AnnualLeave leave = service.getById(id);

        if (leave == null) {
            return createErrorResponse("Leave request not found or deleted.", HttpStatus.NOT_FOUND);
        }

        // If the user is not BOSS/ADMIN, they can only delete their own leave request
        if (!leave.getUser().getId().equals(currentUser.getId()) && !(currentUser.getRole() == Role.BOSS || currentUser.getRole() == Role.ADMIN)) {
            return createErrorResponse("You are not authorized to delete this leave request.", HttpStatus.FORBIDDEN);
        }

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
        errorResponse.put("error", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateLeave(@PathVariable Long id, @RequestBody AnnualLeave updatedLeave) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        AnnualLeave existingLeave = service.getById(id);

        if (existingLeave == null) {
            return createErrorResponse("Leave request not found or deleted.", HttpStatus.NOT_FOUND);
        }

        // If the user is not BOSS/ADMIN, they can only update their own leave request
        if (!existingLeave.getUser().getId().equals(currentUser.getId()) && !(currentUser.getRole() == Role.BOSS || currentUser.getRole() == Role.ADMIN)) {
            return createErrorResponse("You are not authorized to update this leave request.", HttpStatus.FORBIDDEN);
        }

        try {
            AnnualLeave leave = service.update(id, updatedLeave);
            return createResponse(leave, "Leave request updated successfully.", HttpStatus.OK);
        } catch (IllegalArgumentException ex) {
            return createErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
