package wildtrack.example.wildtrackbackend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import wildtrack.example.wildtrackbackend.entity.NASLog;

@Repository
public interface NASLogRepository extends JpaRepository<NASLog, Long> {
    Optional<NASLog> findTopByIdNumberOrderByIdDesc(String idNumber);
}
