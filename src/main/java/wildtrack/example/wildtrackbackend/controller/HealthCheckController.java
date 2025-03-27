package wildtrack.example.wildtrackbackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:5174",
        "https://wild-track.vercel.app",
        "https://wild-track-ejhubs-projects.vercel.app",
        "https://wild-track-mi1773661-ejhubs-projects.vercel.app"
})
public class HealthCheckController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", new Date().toString());
        return ResponseEntity.ok(response);
    }
}