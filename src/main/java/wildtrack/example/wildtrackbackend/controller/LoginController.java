package wildtrack.example.wildtrackbackend.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import wildtrack.example.wildtrackbackend.entity.User;
import wildtrack.example.wildtrackbackend.repository.UserRepository;

@RestController
@RequestMapping("/api")
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
public ResponseEntity<?> login(@RequestBody User loginRequest) {
    try {
        Optional<User> userOptional = userRepository.findByEmail(loginRequest.getEmail());

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                // Return the role from the database
                return ResponseEntity.ok(Map.of(
                    "message", "Login successful",
                    "role", user.getRole() // Fetch role dynamically
                ));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "User not found"));
        }
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("error", "An error occurred while processing the login request"));
    }
}


    /**
     * A placeholder method to determine user role.
     * Replace with actual logic for fetching the user role from the database or another source.
     */
    private String determineUserRole(String email) {
        // Replace this with your actual logic to determine the user's role
        if (email.endsWith("teacher.com")) {
            return "Teacher";
        } else if (email.endsWith("student.com")) {
            return "Student";
        } else {
            return "Librarian";
        }
    }
}
