package wildtrack.example.wildtrackbackend.repository;

import wildtrack.example.wildtrackbackend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // Find notifications for specific user
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Find notifications for a specific grade level
    List<Notification> findByGradeLevelOrderByCreatedAtDesc(String gradeLevel);

    // Find unread notifications for a specific user
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    // Find notifications that are either for a specific user or for their grade
    // level
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId OR (n.gradeLevel = :gradeLevel AND n.userId IS NULL) ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdOrGradeLevel(@Param("userId") Long userId, @Param("gradeLevel") String gradeLevel);

    // Count unread notifications for a user
    @Query("SELECT COUNT(n) FROM Notification n WHERE (n.userId = :userId OR (n.gradeLevel = :gradeLevel AND n.userId IS NULL)) AND n.isRead = false")
    Long countUnreadNotifications(@Param("userId") Long userId, @Param("gradeLevel") String gradeLevel);
}