package wildtrack.example.wildtrackbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wildtrack.example.wildtrackbackend.entity.User;
import wildtrack.example.wildtrackbackend.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teachers") // Manage teachers related API routes
public class ManageTeacherController {

    @Autowired
    private UserService userService;

    // Fetch all teachers (filtered by 'Teacher' role)
    @GetMapping("/all")
    public ResponseEntity<?> getAllTeachers() {
        try {
            List<User> teachers = userService.getUsersByRole("Teacher");
            return ResponseEntity.ok(teachers);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred while fetching teachers."));
        }
    }

    // Add a new teacher with automatic password generation
    @PostMapping("/register")
    public ResponseEntity<?> registerTeacher(@RequestBody User user) {
        try {
            // Check if ID number already exists
            if (userService.isIdNumberExists(user.getIdNumber())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "ID Number already exists."));
            }

            // Ensure role is "Teacher"
            if (!"Teacher".equalsIgnoreCase(user.getRole())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Only teachers can be registered."));
            }

            // Validate teacher-specific fields
            if (user.getSubject() == null || user.getSubject().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Subject is required."));
            }

            // Validate grade and section (these might be comma-separated strings for
            // teachers)
            if (user.getGrade() == null || user.getGrade().isEmpty() ||
                    user.getSection() == null || user.getSection().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Grade and Section are required for teachers."));
            }

            // Save teacher with possibly generated password
            User savedUser = userService.saveUser(user);

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Teacher registered successfully!");

            // Include temporary password if it was auto-generated
            if (savedUser.isPasswordResetRequired()) {
                response.put("temporaryPassword", savedUser.getPassword()); // This is the clear text password
                response.put("passwordResetRequired", true);
            }

            // Don't include password in the user object
            savedUser.setPassword(null);
            response.put("user", savedUser);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            // Return the specific error message from validation
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // Update teacher details
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTeacher(@PathVariable Long id, @RequestBody User user) {
        try {
            // Get existing user to compare ID numbers
            User existingUser = userService.findById(id);
            if (existingUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Teacher not found."));
            }

            // Check if ID number is being changed and if the new ID number already exists
            if (!existingUser.getIdNumber().equals(user.getIdNumber()) &&
                    userService.isIdNumberExists(user.getIdNumber())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "ID Number already exists."));
            }

            // Ensure the teacher has a role of 'Teacher'
            if (!"Teacher".equalsIgnoreCase(user.getRole())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Only teachers can be updated."));
            }

            // Validate teacher-specific fields
            if (user.getSubject() == null || user.getSubject().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Subject is required."));
            }

            User updatedUser = userService.updateUser(id, user);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // Delete a teacher by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTeacher(@PathVariable Long id) {
        try {
            if (!userService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Teacher not found."));
            }

            userService.deleteUserById(id);
            return ResponseEntity.ok(Map.of("message", "Teacher deleted successfully!"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred while deleting the teacher."));
        }
    }
}