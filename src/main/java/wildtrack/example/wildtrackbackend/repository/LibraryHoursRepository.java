package wildtrack.example.wildtrackbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wildtrack.example.wildtrackbackend.entity.LibraryHours;
import java.util.List;
@Repository
public interface LibraryHoursRepository extends JpaRepository<LibraryHours, Long> {
    List<LibraryHours> findByIdNumber(String idNumber);
}