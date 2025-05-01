package wildtrack.example.wildtrackbackend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import wildtrack.example.wildtrackbackend.dto.LibraryHoursWithCreatorDTO;
import wildtrack.example.wildtrackbackend.entity.SetLibraryHours;
import wildtrack.example.wildtrackbackend.entity.User;
import wildtrack.example.wildtrackbackend.repository.UserRepository;
import wildtrack.example.wildtrackbackend.service.SetLibraryHoursService;

@RestController
@RequestMapping("/api/set-library-hours")
public class SetLibraryHoursController {

    @Autowired
    private SetLibraryHoursService service;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<SetLibraryHours> setLibraryHours(@RequestBody SetLibraryHours setLibraryHours) {
        // Get the authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName(); // This will be the user's idNumber

        // Log the incoming data for debugging
        System.out.println("Received library hours task: " + setLibraryHours.getTask());
        System.out.println("Current authenticated user: " + username);

        // Find the user by ID number
        User user = userRepository.findByIdNumber(username).orElse(null);

        if (user != null && "Teacher".equals(user.getRole())) {
            // Set the creator ID
            setLibraryHours.setCreatedById(user.getId());
            System.out.println("Set creator ID: " + user.getId() + " for user: " +
                    (user.getFirstName() != null ? user.getFirstName() : "") + " " +
                    (user.getLastName() != null ? user.getLastName() : ""));
        }

        SetLibraryHours result = service.setLibraryHours(setLibraryHours);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateLibraryHours(
            @PathVariable Long id,
            @RequestBody SetLibraryHours updatedLibraryHours,
            @RequestHeader(value = "X-User-ID", required = false) String userIdHeader) {

        // DETAILED DEBUGGING
        System.out.println("-------------------------");
        System.out.println("UPDATE REQUEST RECEIVED FOR ID: " + id);

        // Get the authenticated user from the security context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String usernameFromAuth = auth.getName();

        System.out.println("Username from auth: " + usernameFromAuth);
        System.out.println("X-User-ID header: " + userIdHeader);

        // Try to get user ID from header first, then fall back to auth context
        String userId = userIdHeader != null ? userIdHeader : usernameFromAuth;
        System.out.println("Using user ID: " + userId);

        // Find user by ID number
        User user = userRepository.findByIdNumber(userId).orElse(null);

        if (user == null) {
            System.out.println("USER NOT FOUND for ID: " + userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "User not found"));
        }

        System.out.println("User found: " + user.getFirstName() + " " + user.getLastName() +
                ", Role: " + user.getRole() + ", ID: " + user.getId());

        // Check if user is authorized (teacher, admin, or librarian)
        if (!"Teacher".equals(user.getRole()) && !"Admin".equals(user.getRole())
                && !"Librarian".equals(user.getRole())) {
            System.out.println("User is not authorized: " + userId + ", role: " + user.getRole());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error",
                            "Only teachers, librarians, and admins can update library hours requirements"));
        }

        // Get the existing requirement
        SetLibraryHours existingRequirement;
        try {
            existingRequirement = service.getLibraryHoursById(id);
            System.out.println("Found existing requirement: " + existingRequirement.getId() +
                    ", Subject: " + existingRequirement.getSubject() +
                    ", Grade: " + existingRequirement.getGradeLevel());
        } catch (Exception e) {
            System.out.println("Error finding requirement: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Requirement not found with ID: " + id));
        }

        try {
            // Preserve the original creator ID
            updatedLibraryHours.setCreatedById(existingRequirement.getCreatedById());

            // Keep approval status
            updatedLibraryHours.setApprovalStatus(existingRequirement.getApprovalStatus());

            System.out.println("Updating library hours requirement...");
            SetLibraryHours result = service.updateLibraryHours(id, updatedLibraryHours);
            System.out.println("Update successful!");

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.out.println("Error updating library hours: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } finally {
            System.out.println("-------------------------");
        }
    }

    @GetMapping
    public ResponseEntity<List<SetLibraryHours>> getAllSetLibraryHours(
            @RequestParam(required = false) String gradeLevel,
            @RequestParam(required = false) String subject) {

        List<SetLibraryHours> hours;

        if (gradeLevel != null) {
            hours = service.getRequirementsForGradeLevel(gradeLevel);

            // Further filter by subject if provided
            if (subject != null && !subject.isEmpty()) {
                hours = hours.stream()
                        .filter(hour -> subject.equals(hour.getSubject()))
                        .toList();
            }
        } else {
            hours = service.getAllSetLibraryHours();
        }

        return ResponseEntity.ok(hours);
    }

    // Endpoint to get all library hours requirements with creator details
    @GetMapping("/all")
    public ResponseEntity<List<LibraryHoursWithCreatorDTO>> getAllLibraryHoursWithCreator(
            @RequestParam(required = false) String gradeLevel) {

        List<LibraryHoursWithCreatorDTO> hoursWithCreator = service.getAllLibraryHoursWithCreator();

        // Filter by grade level if provided
        if (gradeLevel != null && !gradeLevel.isEmpty()) {
            hoursWithCreator = hoursWithCreator.stream()
                    .filter(hour -> gradeLevel.equals(hour.getGradeLevel()))
                    .toList();
        }

        return ResponseEntity.ok(hoursWithCreator);
    }

    // Endpoint to get subjects for a grade level
    @GetMapping("/subjects/{gradeLevel}")
    public ResponseEntity<List<String>> getSubjectsForGrade(@PathVariable String gradeLevel) {
        List<String> subjects = service.getSubjectsForGrade(gradeLevel);
        return ResponseEntity.ok(subjects);
    }

    // Endpoint to get a specific library hours requirement by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getLibraryHoursById(@PathVariable Long id) {
        try {
            SetLibraryHours requirement = service.getLibraryHoursById(id);
            return ResponseEntity.ok(requirement);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}