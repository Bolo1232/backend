package wildtrack.example.wildtrackbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import wildtrack.example.wildtrackbackend.entity.BookLog;

import java.util.List;

@Repository
public interface BookLogRepository extends JpaRepository<BookLog, Long> {
    List<BookLog> findByIdNumber(String idNumber);

    @Query("SELECT bl.bookTitle, COUNT(bl) AS timesRead FROM BookLog bl GROUP BY bl.bookTitle ORDER BY timesRead DESC")
    List<Object[]> findMostReadBooks();

    @Query("SELECT bl.bookTitle, AVG(bl.rating) AS averageRating FROM BookLog bl GROUP BY bl.bookTitle ORDER BY averageRating DESC")
    List<Object[]> findHighestRatedBooks();
}
