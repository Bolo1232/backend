package wildtrack.example.wildtrackbackend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import wildtrack.example.wildtrackbackend.entity.SetLibraryHours;
import wildtrack.example.wildtrackbackend.service.SetLibraryHoursService;

@RestController
@RequestMapping("/api/library-hours-approval")
@CrossOrigin(origins = "http://localhost:5173")
public class LibraryHoursApprovalController {

    @Autowired
    private SetLibraryHoursService service;

    @GetMapping("/pending")
    public ResponseEntity<List<SetLibraryHours>> getPendingApprovals() {
        List<SetLibraryHours> pendingApprovals = service.getPendingApprovals();
        return ResponseEntity.ok(pendingApprovals);
    }

    @PostMapping("/approve/{id}")
    public ResponseEntity<?> approveLibraryHours(@PathVariable Long id) {
        try {
            SetLibraryHours approvedHours = service.approveLibraryHours(id);

            if (approvedHours != null) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Library hours requirement approved successfully",
                        "data", approvedHours));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "success", false,
                        "message", "Library hours requirement not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Error approving library hours requirement: " + e.getMessage()));
        }
    }

    @PostMapping("/reject/{id}")
    public ResponseEntity<?> rejectLibraryHours(
            @PathVariable Long id,
            @RequestBody Map<String, String> requestBody) {
        try {
            String reason = requestBody.getOrDefault("reason", "No reason provided");

            SetLibraryHours rejectedHours = service.rejectLibraryHours(id, reason);

            if (rejectedHours != null) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Library hours requirement rejected successfully",
                        "data", rejectedHours));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "success", false,
                        "message", "Library hours requirement not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Error rejecting library hours requirement: " + e.getMessage()));
        }
    }
}