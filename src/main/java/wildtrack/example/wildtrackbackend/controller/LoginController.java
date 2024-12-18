package wildtrack.example.wildtrackbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wildtrack.example.wildtrackbackend.entity.User;
import wildtrack.example.wildtrackbackend.repository.UserRepository;
import wildtrack.example.wildtrackbackend.service.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;

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
        String idNumber = loginRequest.get("idNumber"); // Use idNumber instead of email
        String password = loginRequest.get("password");

        Optional<User> userOptional = userRepository.findByIdNumber(idNumber);
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            if (passwordEncoder.matches(password, user.getPassword())) {
                String token = tokenService.generateToken(idNumber);

                return ResponseEntity.ok(Map.of(
                        "message", "Login successful",
                        "token", token,
                        "role", user.getRole(),
                        "idNumber", user.getIdNumber()));
            } else {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
            }
        } else {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
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
