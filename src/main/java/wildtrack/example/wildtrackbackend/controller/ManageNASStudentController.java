package wildtrack.example.wildtrackbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import wildtrack.example.wildtrackbackend.entity.User;
import wildtrack.example.wildtrackbackend.service.UserService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/nas-students")
@CrossOrigin(origins = "http://localhost:5173") // Enable CORS for your frontend
public class ManageNASStudentController {

    @Autowired
    private UserService userService;

    // Get all NAS Students
    @GetMapping("/all")
    public ResponseEntity<?> getAllNASStudents() {
        try {
            List<User> nasStudents = userService.getUsersByRole("NAS Student");
            return ResponseEntity.ok(nasStudents);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred while fetching NAS students."));
        }
    }

    // Add a new NAS Student
    @PostMapping("/register")
    public ResponseEntity<?> addNASStudent(@RequestBody User user) {
        try {
            // Validate required fields
            if (user.getFirstName() == null || user.getFirstName().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "First Name is required."));
            }
            if (user.getLastName() == null || user.getLastName().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Last Name is required."));
            }
            if (user.getIdNumber() == null || user.getIdNumber().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "ID Number is required."));
            }
            if (user.getEmail() == null || user.getEmail().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Email is required."));
            }
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Password is required."));
            }
            if (user.getWorkPeriod() == null || user.getWorkPeriod().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Work Period is required."));
            }
            if (user.getAssignedTask() == null || user.getAssignedTask().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Assigned Task is required."));
            }
            // Check if email already exists
            if (userService.isEmailExists(user.getEmail())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Email already exists."));
            }

            // Set role to "NAS Student"
            user.setRole("NAS Student");

            // Encrypt the password
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            user.setPassword(encoder.encode(user.getPassword()));

            // Save the user
            userService.saveUser(user);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "NAS Student added successfully!"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred while adding the NAS student."));
        }
    }

    // Update an NAS Student by ID
    @PutMapping("/{id}")
    public ResponseEntity<?> updateNASStudent(@PathVariable Long id, @RequestBody User user) {
        System.out.println("Received ID: " + id);
        System.out.println("Received User: " + user);
        try {
            if (!"NAS Student".equalsIgnoreCase(user.getRole())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Only NAS students can be updated."));
            }

            User updatedUser = userService.updateUser(id, user);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // Delete an NAS Student by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNASStudent(@PathVariable Long id) {
        try {
            if (!userService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "NAS Student not found."));
            }
            userService.deleteUserById(id);
            return ResponseEntity.ok(Map.of("message", "NAS Student deleted successfully!"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred while deleting the NAS student."));
        }
    }
}
