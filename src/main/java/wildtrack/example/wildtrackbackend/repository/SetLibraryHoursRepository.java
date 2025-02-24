package wildtrack.example.wildtrackbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import wildtrack.example.wildtrackbackend.entity.SetLibraryHours;

@Repository
public interface SetLibraryHoursRepository extends JpaRepository<SetLibraryHours, Long> {
}