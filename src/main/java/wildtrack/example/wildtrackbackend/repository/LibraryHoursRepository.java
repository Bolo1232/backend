package wildtrack.example.wildtrackbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import wildtrack.example.wildtrackbackend.entity.LibraryHours;

import java.util.List;
import java.util.Optional;

@Repository
public interface LibraryHoursRepository extends JpaRepository<LibraryHours, Long> {
    List<LibraryHours> findByIdNumber(String idNumber);

    @Query("SELECT lh FROM LibraryHours lh WHERE lh.idNumber = :idNumber ORDER BY lh.timeIn DESC")
    List<LibraryHours> findAllByIdNumberOrdered(@Param("idNumber") String idNumber);

    default Optional<LibraryHours> findLatestByIdNumber(String idNumber) {
        List<LibraryHours> results = findAllByIdNumberOrdered(idNumber);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}
