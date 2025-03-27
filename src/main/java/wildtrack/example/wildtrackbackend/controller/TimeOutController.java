package wildtrack.example.wildtrackbackend.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import wildtrack.example.wildtrackbackend.entity.LibraryHours;
import wildtrack.example.wildtrackbackend.entity.User;
import wildtrack.example.wildtrackbackend.service.TimeOutService;
import wildtrack.example.wildtrackbackend.service.UserService;

@RestController
@RequestMapping("/api/time-out")
public class TimeOutController {

    @Autowired
    private TimeOutService timeOutService;

    @Autowired
    private UserService userService;

    /**
     * Record time-out for a student
     */
    @PostMapping
    public ResponseEntity<?> recordTimeOut(@RequestBody Map<String, String> request) {
        String idNumber = request.get("idNumber");
        try {
            User user = userService.findByIdNumber(idNumber);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Student not found."));
            }

            LibraryHours hours = timeOutService.recordTimeOut(idNumber);
            return ResponseEntity.ok(Map.of(
                    "message", "Time-out recorded successfully.",
                    "student", user,
                    "libraryHours", hours));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred."));
        }
    }

    /**
     * Record time-out with subject selection for a student
     */
    @PutMapping("/with-subject")
    public ResponseEntity<?> recordTimeOutWithSubject(@RequestBody Map<String, String> request) {
        String idNumber = request.get("idNumber");
        String subject = request.get("subject");

        try {
            User user = userService.findByIdNumber(idNumber);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Student not found."));
            }

            LibraryHours hours = timeOutService.recordTimeOutWithSubject(idNumber, subject);

            return ResponseEntity.ok(Map.of(
                    "message", "Time-out with subject recorded successfully.",
                    "student", user,
                    "libraryHours", hours));
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