package wildtrack.example.wildtrackbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wildtrack.example.wildtrackbackend.entity.User;
import wildtrack.example.wildtrackbackend.service.UserService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/students") // Manage students related API routes
@CrossOrigin(origins = "http://localhost:5173") // Enable CORS for frontend
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

    // Add a new student
    @PostMapping("/register")
    public ResponseEntity<?> registerStudent(@RequestBody User user) {
        try {
            if (userService.isEmailExists(user.getEmail())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Email already exists."));
            }

            // Ensure the user has the role 'Student'
            if (!"Student".equalsIgnoreCase(user.getRole())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Only students can be registered."));
            }

            // Validate required fields for students
            if (user.getGrade() == null || user.getSection() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Grade and Section are required for students."));
            }

            userService.saveUser(user);
            return ResponseEntity.ok(Map.of("message", "Student registered successfully!"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred while registering student."));
        }
    }

    // Update student details
    @PutMapping("/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable Long id, @RequestBody User user) {
        try {
            // Ensure the user has the role 'Student'
            if (!"Student".equalsIgnoreCase(user.getRole())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Only students can be updated."));
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
