package wildtrack.example.wildtrackbackend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import wildtrack.example.wildtrackbackend.entity.SetLibraryHours;
import wildtrack.example.wildtrackbackend.entity.User;
import wildtrack.example.wildtrackbackend.repository.UserRepository;
import wildtrack.example.wildtrackbackend.service.SetLibraryHoursService;

@RestController
@RequestMapping("/api/set-library-hours")
@CrossOrigin(origins = "http://localhost:5173")
public class SetLibraryHoursController {

    @Autowired
    private SetLibraryHoursService service;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<SetLibraryHours> setLibraryHours(@RequestBody SetLibraryHours setLibraryHours) {
        // Get the authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName(); // This will be the user's idNumber or email

        // Find the user by ID number or email
        User user = userRepository.findByIdNumber(username)
                .orElseGet(() -> userRepository.findByEmail(username).orElse(null));

        if (user != null && "Teacher".equals(user.getRole())) {
            // Set the creator ID
            setLibraryHours.setCreatedById(user.getId());
        }

        SetLibraryHours result = service.setLibraryHours(setLibraryHours);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<SetLibraryHours>> getAllSetLibraryHours() {
        List<SetLibraryHours> hours = service.getAllSetLibraryHours();
        return ResponseEntity.ok(hours);
    }

    // New endpoint to get only approved requirements for a grade level
    @GetMapping("/approved/{gradeLevel}")
    public ResponseEntity<List<SetLibraryHours>> getApprovedRequirements(@PathVariable String gradeLevel) {
        List<SetLibraryHours> approvedHours = service.getApprovedRequirements(gradeLevel);
        return ResponseEntity.ok(approvedHours);
    }
}