package wildtrack.example.wildtrackbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import wildtrack.example.wildtrackbackend.entity.Journal;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface JournalRepository extends JpaRepository<Journal, Long> {

    // Find journals by user's ID number
    List<Journal> findByIdNumber(String idNumber);

    // Find journals by user's ID number and date range
    List<Journal> findByIdNumberAndDateReadBetween(String idNumber, LocalDate startDate, LocalDate endDate);

    // Find journals by user's ID number and activity
    List<Journal> findByIdNumberAndActivity(String idNumber, String activity);

    // Find most recent journal entry for a user
    Journal findFirstByIdNumberOrderByDateReadDesc(String idNumber);

    // Find journal with highest entry number
    @Query("SELECT MAX(CAST(j.entryNo AS int)) FROM Journal j")
    Optional<Integer> findMaxEntryNumber();

    // Analytics queries
    @Query("SELECT j.details, COUNT(j) AS timesRead FROM Journal j WHERE j.activity = 'Read Book' GROUP BY j.details ORDER BY timesRead DESC")
    List<Object[]> findMostReadBooks();

    @Query("SELECT j.details, AVG(j.rating) AS averageRating FROM Journal j WHERE j.activity = 'Read Book' GROUP BY j.details ORDER BY averageRating DESC")
    List<Object[]> findHighestRatedBooks();

    // Find journals by book title (now using details field)
    List<Journal> findByDetailsContainingIgnoreCase(String bookTitle);

    // Count journals by activity
    Long countByIdNumberAndActivity(String idNumber, String activity);

    // Generate analytics for reading activity over time
    @Query("SELECT FUNCTION('YEAR', j.dateRead) as year, FUNCTION('MONTH', j.dateRead) as month, COUNT(j) as count " +
            "FROM Journal j WHERE j.idNumber = ?1 AND j.activity IN ('Read Book', 'Read Periodical') " +
            "GROUP BY FUNCTION('YEAR', j.dateRead), FUNCTION('MONTH', j.dateRead) " +
            "ORDER BY year, month")
    List<Object[]> getReadingActivityByMonth(String idNumber);

    // Find journals with specific ratings
    List<Journal> findByIdNumberAndRatingGreaterThanEqual(String idNumber, int minRating);
}