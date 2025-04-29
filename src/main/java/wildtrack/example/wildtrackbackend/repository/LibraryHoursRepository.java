package wildtrack.example.wildtrackbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import wildtrack.example.wildtrackbackend.entity.LibraryHours;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LibraryHoursRepository extends JpaRepository<LibraryHours, Long> {
        List<LibraryHours> findByIdNumber(String idNumber);

        // Find library hours for a specific student, ordered by time-in descending
        List<LibraryHours> findByIdNumberOrderByTimeInDesc(String idNumber);

        // Find pending time-in records (where time-out is null)
        List<LibraryHours> findByIdNumberAndTimeOutIsNullOrderByTimeInDesc(String idNumber);

        // Find library hours for a specific subject
        List<LibraryHours> findByIdNumberAndSubject(String idNumber, String subject);

        // Add this method to find all records with null timeOut
        List<LibraryHours> findByTimeOutIsNull();

        // Find library hours associated with a specific requirement
        List<LibraryHours> findByIdNumberAndRequirementId(String idNumber, Long requirementId);

        // Count minutes rendered for a specific requirement
        @Query("SELECT SUM(lh.minutesCounted) FROM LibraryHours lh WHERE lh.idNumber = ?1 AND lh.requirementId = ?2 AND lh.isCounted = true")
        Integer countMinutesForRequirement(String idNumber, Long requirementId);

        // Get total minutes by subject and quarter
        @Query("SELECT lh.subject, FUNCTION('QUARTER', lh.timeIn), SUM(lh.minutesCounted) " +
                        "FROM LibraryHours lh " +
                        "WHERE lh.idNumber = ?1 AND lh.timeOut IS NOT NULL AND lh.isCounted = true " +
                        "GROUP BY lh.subject, FUNCTION('QUARTER', lh.timeIn)")
        List<Object[]> getTotalMinutesBySubjectAndQuarter(String idNumber);

        // Find library hours within a date range
        List<LibraryHours> findByIdNumberAndTimeInBetween(String idNumber, LocalDateTime startDate,
                        LocalDateTime endDate);

        List<LibraryHours> findByTimeInBetweenAndTimeOutIsNotNull(LocalDateTime startDate, LocalDateTime endDate);

        long countByTimeOutIsNotNullAndTimeInBetween(LocalDateTime startDate, LocalDateTime endDate);

        @Query("SELECT lh FROM LibraryHours lh WHERE lh.idNumber = :idNumber ORDER BY lh.timeIn DESC")
        List<LibraryHours> findAllByIdNumberOrdered(@Param("idNumber") String idNumber);

        default Optional<LibraryHours> findLatestByIdNumber(String idNumber) {
                List<LibraryHours> results = findAllByIdNumberOrdered(idNumber);
                return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        }

        long countByIdNumberInAndTimeOutIsNotNullAndTimeInBetween(
                        List<String> idNumbers, LocalDateTime startDate, LocalDateTime endDate);

        @Query("SELECT COUNT(DISTINCT h.idNumber) FROM LibraryHours h WHERE h.timeOut IS NOT NULL AND h.timeIn BETWEEN ?1 AND ?2")
        long countDistinctUsersByTimeInBetween(LocalDateTime startTime, LocalDateTime endTime);

        @Query("SELECT COUNT(DISTINCT h.idNumber) FROM LibraryHours h WHERE h.idNumber IN ?1 AND h.timeOut IS NOT NULL AND h.timeIn BETWEEN ?2 AND ?3")
        long countDistinctUsersByIdNumberInAndTimeInBetween(List<String> idNumbers, LocalDateTime startTime,
                        LocalDateTime endTime);

}