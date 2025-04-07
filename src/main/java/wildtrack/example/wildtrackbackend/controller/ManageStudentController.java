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
@RequestMapping("/api/students") // Manage students related API routes
public class ManageStudentController {

    @Autowired
    private UserService userService;

    // Fetch all students (filtered by 'Student' role)
    @GetMapping("/all")
    public ResponseEntity<?> getAllStudents() {
        try {
            List<User> students = userService.getUsersByRole("Student");
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred while fetching students."));
        }
    }

    // Add a new student with automatic password generation
    @PostMapping("/register")
    public ResponseEntity<?> registerStudent(@RequestBody User user) {
        try {
            // Check if ID number already exists
            if (userService.isIdNumberExists(user.getIdNumber())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "ID Number already exists."));
            }

            // Ensure the user has the role 'Student'
            if (!"Student".equalsIgnoreCase(user.getRole())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Only students can be registered."));
            }

            // Validate required fields for students
            if (user.getGrade() == null || user.getSection() == null || user.getAcademicYear() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Grade, Section, and Academic Year are required for students."));
            }

            // Save user with possibly generated password
            User savedUser = userService.saveUser(user);

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Student registered successfully!");

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

    // Update student details
    @PutMapping("/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable Long id, @RequestBody User user) {
        try {
            // Get existing user to compare ID numbers
            User existingUser = userService.findById(id);
            if (existingUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Student not found."));
            }

            // Check if ID number is being changed and if the new ID number already exists
            if (!existingUser.getIdNumber().equals(user.getIdNumber()) &&
                    userService.isIdNumberExists(user.getIdNumber())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "ID Number already exists."));
            }

            // Ensure the user has the role 'Student'
            if (!"Student".equalsIgnoreCase(user.getRole())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Only students can be updated."));
            }

            // Validate required fields for students
            if (user.getGrade() == null || user.getSection() == null || user.getAcademicYear() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Grade, Section, and Academic Year are required for students."));
            }

            User updatedUser = userService.updateUser(id, user);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // Delete a student by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable Long id) {
        try {
            if (!userService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Student not found."));
            }

            userService.deleteUserById(id);
            return ResponseEntity.ok(Map.of("message", "Student deleted successfully!"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred while deleting the student."));
        }
    }
}