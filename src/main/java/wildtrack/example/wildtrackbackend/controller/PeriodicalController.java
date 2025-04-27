package wildtrack.example.wildtrackbackend.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import wildtrack.example.wildtrackbackend.entity.Periodical;
import wildtrack.example.wildtrackbackend.service.PeriodicalService;

@RestController
@RequestMapping("/api/periodicals")
public class PeriodicalController {

    @Autowired
    private PeriodicalService periodicalService;

    // Add a new periodical
    @PostMapping("/add")
    public ResponseEntity<?> addPeriodical(@RequestBody Periodical periodical) {
        try {
            if (periodicalService.existsByAccessionNumber(periodical.getAccessionNumber())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Accession number already exists.");
            }

            // If dateRegistered isn't set, it will be automatically set by @PrePersist
            Periodical savedPeriodical = periodicalService.savePeriodical(periodical);
            return ResponseEntity.ok(savedPeriodical);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while adding the periodical.");
        }
    }

    // Retrieve all periodicals
    @GetMapping("/all")
    public ResponseEntity<List<Periodical>> getAllPeriodicals() {
        try {
            List<Periodical> periodicals = periodicalService.getAllPeriodicals();
            return ResponseEntity.ok(periodicals);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Retrieve a specific periodical by its ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getPeriodicalById(@PathVariable Long id) {
        try {
            Optional<Periodical> periodicalOptional = periodicalService.getPeriodicalById(id);

            if (periodicalOptional.isPresent()) {
                return ResponseEntity.ok(periodicalOptional.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Periodical not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred.");
        }
    }

    // Update an existing periodical
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePeriodical(@PathVariable Long id, @RequestBody Periodical periodical) {
        try {
            Optional<Periodical> updatedPeriodical = periodicalService.updatePeriodical(id, periodical);

            if (updatedPeriodical.isPresent()) {
                return ResponseEntity.ok(updatedPeriodical.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Periodical not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred.");
        }
    }

    // Delete a periodical by its ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePeriodical(@PathVariable Long id) {
        try {
            periodicalService.getPeriodicalById(id)
                    .orElseThrow(() -> new RuntimeException("Periodical not found."));
            periodicalService.deletePeriodical(id);
            return ResponseEntity.ok(Map.of("message", "Periodical deleted successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred."));
        }
    }
}