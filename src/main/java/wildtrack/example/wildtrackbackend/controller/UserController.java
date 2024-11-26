package wildtrack.example.wildtrackbackend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import wildtrack.example.wildtrackbackend.entity.User;
import wildtrack.example.wildtrackbackend.service.UserService;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173") // Enable CORS for your frontend
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
public ResponseEntity<?> registerUser(@RequestBody User user) {
    System.out.println("Received User: " + user);
    try {
        if (userService.isEmailExists(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Email already exists."));
        }
        userService.saveUser(user);
        return ResponseEntity.ok(Map.of("message", "User registered successfully!"));
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An error occurred while registering the user."));
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
}
