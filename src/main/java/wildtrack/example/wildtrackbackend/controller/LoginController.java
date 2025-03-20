package wildtrack.example.wildtrackbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wildtrack.example.wildtrackbackend.entity.User;
import wildtrack.example.wildtrackbackend.repository.UserRepository;
import wildtrack.example.wildtrackbackend.service.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenService tokenService;

    // Login endpoint (using idNumber)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String idNumber = loginRequest.get("idNumber");
        String password = loginRequest.get("password");

        Optional<User> userOptional = userRepository.findByIdNumber(idNumber);
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            if (passwordEncoder.matches(password, user.getPassword())) {
                String token = tokenService.generateToken(idNumber);

                // Create a response map with all needed fields
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("message", "Login successful");
                responseMap.put("token", token);
                responseMap.put("role", user.getRole());
                responseMap.put("idNumber", user.getIdNumber());
                responseMap.put("userId", user.getId());
                responseMap.put("requirePasswordChange", user.isPasswordResetRequired());

                return ResponseEntity.ok(responseMap);
            } else {
                // Use consistent error format
                Map<String, Object> errorMap = new HashMap<>();

                if (user.isPasswordResetRequired()) {
                    errorMap.put("error", "temporary_password_incorrect");
                    errorMap.put("message",
                            "Incorrect temporary password. Please request the correct temporary password from the Librarian.");
                } else {
                    errorMap.put("error", "invalid_credentials");
                    errorMap.put("message", "Invalid credentials");
                }

                return ResponseEntity.status(401).body(errorMap);
            }
        } else {
            return ResponseEntity.status(404).body(Map.of(
                    "error", "user_not_found",
                    "message", "User not found"));
        }
    }

    // Verify token endpoint
    @GetMapping("/verify-token")
    public ResponseEntity<?> verifyToken(@RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Authorization header missing or invalid"));
        }

        String token = authorizationHeader.replace("Bearer ", "");

        try {
            String idNumber = tokenService.verifyToken(token);
            Optional<User> userOptional = userRepository.findByIdNumber(idNumber);

            if (userOptional.isPresent()) {
                User user = userOptional.get();

                return ResponseEntity.ok(Map.of(
                        "user", Map.of(
                                "idNumber", user.getIdNumber(),
                                "role", user.getRole())));
            } else {
                return ResponseEntity.status(404).body(Map.of("error", "User not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }
    }
}
