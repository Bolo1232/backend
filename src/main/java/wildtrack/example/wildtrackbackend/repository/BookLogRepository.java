package wildtrack.example.wildtrackbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import wildtrack.example.wildtrackbackend.entity.BookLog;

import java.util.List;

@Repository
public interface BookLogRepository extends JpaRepository<BookLog, Long> {
    List<BookLog> findByIdNumber(String idNumber);
}
