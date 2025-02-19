package wildtrack.example.wildtrackbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wildtrack.example.wildtrackbackend.entity.LibraryHours;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeInRepository extends JpaRepository<LibraryHours, Long> {

    // Find active session for a student (not timed out)
    Optional<LibraryHours> findByIdNumberAndTimeOutIsNull(String idNumber);

    // Count current active students
    long countByTimeOutIsNull();

    // Check if student has an active session
    boolean existsByIdNumberAndTimeOutIsNull(String idNumber);

    // Get all active sessions
    List<LibraryHours> findByTimeOutIsNull();

    // Find time-in records for a specific student
    List<LibraryHours> findByIdNumber(String idNumber);
}