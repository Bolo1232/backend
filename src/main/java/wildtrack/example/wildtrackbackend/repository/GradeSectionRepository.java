package wildtrack.example.wildtrackbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wildtrack.example.wildtrackbackend.entity.GradeSection;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GradeSectionRepository extends JpaRepository<GradeSection, Long> {
    List<GradeSection> findByGradeLevel(String gradeLevel);

    List<GradeSection> findByStatus(String status);

    boolean existsByGradeLevelAndSectionName(String gradeLevel, String sectionName);
}