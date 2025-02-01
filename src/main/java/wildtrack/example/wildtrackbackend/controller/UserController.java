package wildtrack.example.wildtrackbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wildtrack.example.wildtrackbackend.entity.User;
import wildtrack.example.wildtrackbackend.service.LibraryHoursService;
import wildtrack.example.wildtrackbackend.service.UserService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173") // Enable CORS for your frontend
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private LibraryHoursService libraryHoursService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            if (userService.isEmailExists(user.getEmail())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Email already exists."));
            }

            // Validate fields based on role
            if ("Student".equalsIgnoreCase(user.getRole())) {
                if (user.getGrade() == null || user.getSection() == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "Grade and Section are required for students."));
                }
            }

            userService.saveUser(user);
            return ResponseEntity.ok(Map.of("message", "User registered successfully!"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An error occurred."));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "An error occurred while fetching users."));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            User updatedUser = userService.updateUser(id, user);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            if (!userService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found."));
            }
            userService.deleteUserById(id);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully!"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred while deleting the user."));
        }
    }

    @GetMapping("/{idNumber}")
    public ResponseEntity<?> getUserByIdNumber(@PathVariable String idNumber) {
        try {
            User user = userService.getUserByIdNumber(idNumber);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found."));
            }
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An error occurred."));
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> requestBody) {
        try {
            Long id = Long.parseLong(requestBody.get("id"));
            String currentPassword = requestBody.get("currentPassword");
            String newPassword = requestBody.get("newPassword");

            userService.changePassword(id, currentPassword, newPassword);
            return ResponseEntity.ok(Map.of("message", "Password changed successfully!"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

}
