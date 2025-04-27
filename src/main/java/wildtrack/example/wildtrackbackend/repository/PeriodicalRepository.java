package wildtrack.example.wildtrackbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import wildtrack.example.wildtrackbackend.entity.Periodical;

@Repository
public interface PeriodicalRepository extends JpaRepository<Periodical, Long> {
    boolean existsByAccessionNumber(String accessionNumber);
}