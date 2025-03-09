package wildtrack.example.wildtrackbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wildtrack.example.wildtrackbackend.entity.Report;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByUserId(Long userId);

    List<Report> findByUserIdNumber(String idNumber);

    List<Report> findByStatus(String status);

    List<Report> findByRole(String role);
}