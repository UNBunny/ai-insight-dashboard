package com.example.demo.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for admin-specific endpoints that can be used to test
 * role-based access control functionality
 */
@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    /**
     * Simple endpoint to check admin access and return server status information
     * @return Status information
     */
    @GetMapping("/status")
    public ResponseEntity<?> getAdminStatus() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> status = new HashMap<>();
        status.put("timestamp", LocalDateTime.now().toString());
        status.put("serverStatus", "operational");
        status.put("username", authentication.getName());
        status.put("authorities", authentication.getAuthorities());
        status.put("adminAccess", true);
        status.put("message", "You have successfully accessed an admin-only endpoint");
        
        return ResponseEntity.ok(status);
    }
    
    /**
     * Returns system statistics that only admins should be able to view
     * @return System statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("activeUsers", 42);
        stats.put("totalRequests", 1337);
        stats.put("cpuUsage", "23.5%");
        stats.put("memoryUsage", "512MB");
        stats.put("diskSpace", "10.2GB free");
        
        return ResponseEntity.ok(stats);
    }
}
