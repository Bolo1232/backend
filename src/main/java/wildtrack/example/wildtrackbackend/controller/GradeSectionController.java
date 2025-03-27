package wildtrack.example.wildtrackbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wildtrack.example.wildtrackbackend.entity.GradeSection;
import wildtrack.example.wildtrackbackend.service.GradeSectionService;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/grade-sections")

public class GradeSectionController {

    @Autowired
    private GradeSectionService gradeSectionService;

    @PostMapping("/add")
    public ResponseEntity<?> addGradeSection(@RequestBody GradeSection gradeSection) {
        try {
            GradeSection savedGradeSection = gradeSectionService.addGradeSection(gradeSection);
            return ResponseEntity.ok(savedGradeSection);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<GradeSection>> getAllGradeSections() {
        return ResponseEntity.ok(gradeSectionService.getAllGradeSections());
    }

    @GetMapping("/active")
    public ResponseEntity<List<GradeSection>> getActiveGradeSections() {
        return ResponseEntity.ok(gradeSectionService.getActiveGradeSections());
    }

    @GetMapping("/grade/{gradeLevel}/active")
    public ResponseEntity<List<GradeSection>> getActiveSectionsByGrade(
            @PathVariable String gradeLevel) {
        return ResponseEntity.ok(gradeSectionService.getActiveSectionsByGrade(gradeLevel));
    }

    @GetMapping("/grade/{gradeLevel}")
    public ResponseEntity<List<GradeSection>> getGradeSectionsByGrade(
            @PathVariable String gradeLevel) {
        return ResponseEntity.ok(gradeSectionService.getGradeSectionsByGrade(gradeLevel));
    }

    @PutMapping("/{id}/archive")
    public ResponseEntity<?> archiveGradeSection(@PathVariable Long id) {
        try {
            gradeSectionService.archiveGradeSection(id);
            return ResponseEntity.ok(Map.of("message", "Grade section archived successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{sectionId}/toggle-archive")
    public ResponseEntity<?> toggleArchiveStatus(@PathVariable Long sectionId) {
        try {
            GradeSection section = gradeSectionService.toggleArchiveStatus(sectionId);
            return ResponseEntity.ok(section);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to toggle archive status"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateGradeSection(
            @PathVariable Long id,
            @RequestBody GradeSection gradeSection) {
        try {
            GradeSection updated = gradeSectionService.updateGradeSection(id, gradeSection);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/grade/{gradeLevel}/toggle-archive")
    public ResponseEntity<?> toggleGradeArchiveStatus(@PathVariable String gradeLevel) {
        try {
            List<GradeSection> sections = gradeSectionService.toggleGradeArchiveStatus(gradeLevel);
            return ResponseEntity.ok(sections);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to toggle archive status for grade"));
        }
    }
}