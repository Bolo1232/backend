package wildtrack.example.wildtrackbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wildtrack.example.wildtrackbackend.entity.GradeSection;
import wildtrack.example.wildtrackbackend.repository.GradeSectionRepository;
import java.util.List;

@Service
public class GradeSectionService {

    @Autowired
    private GradeSectionRepository gradeSectionRepository;

    public GradeSection toggleArchiveStatus(Long sectionId) {
        GradeSection section = gradeSectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found"));

        // Toggle the status between 'active' and 'archived'
        section.setStatus(
                section.getStatus().equals("active") ? "archived" : "active");

        return gradeSectionRepository.save(section);
    }

    public GradeSection addGradeSection(GradeSection gradeSection) {
        if (gradeSectionRepository.existsByGradeLevelAndSectionName(
                gradeSection.getGradeLevel(),
                gradeSection.getSectionName())) {
            throw new RuntimeException("This section already exists in the specified grade");
        }

        gradeSection.setStatus("active");
        return gradeSectionRepository.save(gradeSection);
    }

    public List<GradeSection> getAllGradeSections() {
        return gradeSectionRepository.findAll();
    }

    public List<GradeSection> getActiveGradeSections() {
        return gradeSectionRepository.findByStatus("active");
    }

    public List<GradeSection> getGradeSectionsByGrade(String gradeLevel) {
        return gradeSectionRepository.findByGradeLevel(gradeLevel);
    }

    public void archiveGradeSection(Long id) {
        GradeSection gradeSection = gradeSectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Grade section not found"));

        gradeSection.setStatus("archived");
        gradeSectionRepository.save(gradeSection);
    }

    public GradeSection updateGradeSection(Long id, GradeSection updatedGradeSection) {
        GradeSection existingGradeSection = gradeSectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Grade section not found"));

        existingGradeSection.setAdvisor(updatedGradeSection.getAdvisor());
        existingGradeSection.setNumberOfStudents(updatedGradeSection.getNumberOfStudents());

        return gradeSectionRepository.save(existingGradeSection);
    }
}