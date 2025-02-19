package wildtrack.example.wildtrackbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wildtrack.example.wildtrackbackend.entity.AcademicYear;

public interface AcademicYearRepository extends JpaRepository<AcademicYear, Long> {
}