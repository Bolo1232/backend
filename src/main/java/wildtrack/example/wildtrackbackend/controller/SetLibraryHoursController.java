package wildtrack.example.wildtrackbackend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import wildtrack.example.wildtrackbackend.entity.SetLibraryHours;
import wildtrack.example.wildtrackbackend.service.SetLibraryHoursService;

@RestController
@RequestMapping("/api/set-library-hours")
@CrossOrigin(origins = "http://localhost:3000")
public class SetLibraryHoursController {

    @Autowired
    private SetLibraryHoursService service;

    @PostMapping
    public ResponseEntity<SetLibraryHours> setLibraryHours(@RequestBody SetLibraryHours setLibraryHours) {
        SetLibraryHours result = service.setLibraryHours(setLibraryHours);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<SetLibraryHours>> getAllSetLibraryHours() {
        List<SetLibraryHours> hours = service.getAllSetLibraryHours();
        return ResponseEntity.ok(hours);
    }
}
