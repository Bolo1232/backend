package wildtrack.example.wildtrackbackend.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import wildtrack.example.wildtrackbackend.entity.User;
import wildtrack.example.wildtrackbackend.service.TimeInService;
import wildtrack.example.wildtrackbackend.service.UserService;

@RestController
@RequestMapping("/api/time-in")
@CrossOrigin(origins = { "http://localhost:5173", "https://wild-track.vercel.app",
        "https://wild-track-ejhubs-projects.vercel.app" })
public class TimeInController {

    @Autowired
    private TimeInService timeInService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> recordTimeIn(@RequestBody Map<String, String> request) {
        String idNumber = request.get("idNumber");
        try {
            User user = userService.findByIdNumber(idNumber);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Student not found."));
            }

            timeInService.recordTimeIn(idNumber);
            return ResponseEntity.ok(Map.of(
                    "message", "Time-in recorded successfully.",
                    "student", user));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred."));
        }
    }
}