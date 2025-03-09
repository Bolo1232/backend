package wildtrack.example.wildtrackbackend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import wildtrack.example.wildtrackbackend.entity.ActivityLog;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    // Find logs by user ID
    List<ActivityLog> findByUserId(Long userId);

    // Find logs by user role
    List<ActivityLog> findByUserRole(String userRole);

    // Find logs by activity type
    List<ActivityLog> findByActivity(String activity);

    // Find logs by academic year
    List<ActivityLog> findByAcademicYear(String academicYear);

    // Find logs between two dates
    List<ActivityLog> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find logs by academic year and between two dates
    List<ActivityLog> findByAcademicYearAndTimestampBetween(
            String academicYear, LocalDateTime startDate, LocalDateTime endDate);

    // Search logs by description containing a keyword
    @Query("SELECT a FROM ActivityLog a WHERE " +
            "LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.userName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.activity) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<ActivityLog> searchByKeyword(@Param("keyword") String keyword);

    // Complex search with multiple filters
    @Query("SELECT a FROM ActivityLog a WHERE " +
            "(:academicYear IS NULL OR a.academicYear = :academicYear) AND " +
            "(:startDate IS NULL OR a.timestamp >= :startDate) AND " +
            "(:endDate IS NULL OR a.timestamp <= :endDate) AND " +
            "(:keyword IS NULL OR " +
            "LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.userName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.activity) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<ActivityLog> findByFilters(
            @Param("academicYear") String academicYear,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("keyword") String keyword);
}