package wildtrack.example.wildtrackbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wildtrack.example.wildtrackbackend.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email); // Check if email already exists

    long countByRole(String role);

    Optional<User> findByEmail(String email); // Find user by email

    Optional<User> findByIdNumber(String idNumber); // Find user by ID number

    List<User> findByRole(String role); // Fetch users by their role (e.g., "Student")

    List<User> findByRoleAndGrade(String role, String grade); // Fetch users by role and grade

    List<User> findByQuarter(String quarter); // Fetch teachers by quarter

    List<User> findBySubject(String subject); // Fetch teachers by subject

    List<User> findByQuarterAndSubject(String quarter, String subject);

    List<User> findByGrade(String grade);

    // New methods for filtering
    List<User> findBySection(String section); // Fetch students by section

    List<User> findByGradeAndSection(String grade, String section); // Fetch students by grade and section

    // Find users by grade and role
    List<User> findByGradeAndRole(String grade, String role);
}