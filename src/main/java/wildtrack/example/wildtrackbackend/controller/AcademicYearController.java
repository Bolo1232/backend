package wildtrack.example.wildtrackbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wildtrack.example.wildtrackbackend.entity.AcademicYear;
import wildtrack.example.wildtrackbackend.service.AcademicYearService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/academic-years")
@CrossOrigin(origins = "http://localhost:5173")
public class AcademicYearController {

    @Autowired
    private AcademicYearService academicYearService;

    @GetMapping("/all")
    public ResponseEntity<?> getAllAcademicYears() {
        try {
            List<AcademicYear> academicYears = academicYearService.getAllAcademicYears();
            return ResponseEntity.ok(academicYears);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred while fetching academic years."));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createAcademicYear(@RequestBody AcademicYear academicYear) {
        try {
            AcademicYear savedAcademicYear = academicYearService.saveAcademicYear(academicYear);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedAcademicYear);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred while creating the academic year."));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAcademicYear(@PathVariable Long id, @RequestBody AcademicYear academicYear) {
        try {
            AcademicYear updatedAcademicYear = academicYearService.updateAcademicYear(id, academicYear);
            return ResponseEntity.ok(updatedAcademicYear);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAcademicYear(@PathVariable Long id) {
        try {
            if (!academicYearService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Academic year not found."));
            }

            academicYearService.deleteAcademicYearById(id);
            return ResponseEntity.ok(Map.of("message", "Academic year deleted successfully!"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred while deleting the academic year."));
        }
    }
}