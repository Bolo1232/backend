package wildtrack.example.wildtrackbackend.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import wildtrack.example.wildtrackbackend.entity.NASLog;
import wildtrack.example.wildtrackbackend.service.NASLogService;

@RestController
@RequestMapping("/api/nas-logs")
@CrossOrigin(origins = "http://localhost:5173") // Allow frontend communication
public class NASLogController {

    @Autowired
    private NASLogService nasLogService;

    @PostMapping("/time-in")
    public ResponseEntity<NASLog> timeIn(@RequestBody Map<String, String> request) {
        String idNumber = request.get("idNumber");

        NASLog log = nasLogService.timeIn(idNumber);
        return ResponseEntity.ok(log);
    }

    @PostMapping("/time-out")
    public ResponseEntity<NASLog> timeOut(@RequestBody Map<String, String> request) {
        String idNumber = request.get("idNumber");

        NASLog log = nasLogService.timeOut(idNumber);
        return ResponseEntity.ok(log);
    }

    @GetMapping("/latest/{idNumber}")
    public ResponseEntity<NASLog> getLatestLog(@PathVariable String idNumber) {
        NASLog log = nasLogService.getLatestLog(idNumber);
        return ResponseEntity.ok(log);
    }
}
