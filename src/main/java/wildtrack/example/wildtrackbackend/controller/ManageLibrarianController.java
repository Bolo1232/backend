
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
@RequestMapping("/api/librarians") // Manage librarians related API routes
@CrossOrigin(origins = "http://localhost:5173") // Enable CORS for frontend
public class ManageLibrarianController {

    @Autowired
    private UserService userService;

    // Fetch all librarians (filtered by 'Librarian' role)
    @GetMapping("/all")
    public ResponseEntity<?> getAllLibrarians() {
        try {
            List<User> librarians = userService.getUsersByRole("Librarian"); // Reusing the method that retrieves users by role
            return ResponseEntity.ok(librarians);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred while fetching librarians."));
        }
    }

    // Add a new librarian
    @PostMapping("/register")
    public ResponseEntity<?> registerLibrarian(@RequestBody User user) {
        try {
            // Validate if email already exists
            if (userService.isEmailExists(user.getEmail())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Email already exists."));
            }

            // Ensure role is "Librarian"
            if (!"Librarian".equalsIgnoreCase(user.getRole())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Only librarians can be registered."));
            }

            // Save librarian
            userService.saveUser(user);
            return ResponseEntity.ok(Map.of("message", "Librarian registered successfully!"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred while registering librarian."));
        }
    }

    // Update librarian details
    @PutMapping("/{id}")
    public ResponseEntity<?> updateLibrarian(@PathVariable Long id, @RequestBody User user) {
        try {
            // Ensure the librarian has a role of 'Librarian'
            if (!"Librarian".equalsIgnoreCase(user.getRole())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Only librarians can be updated."));
            }

            User updatedUser = userService.updateUser(id, user);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // Delete a librarian by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLibrarian(@PathVariable Long id) {
        try {
            if (!userService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Librarian not found."));
            }

            userService.deleteUserById(id);
            return ResponseEntity.ok(Map.of("message", "Librarian deleted successfully!"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred while deleting the librarian."));
        }
    }
}