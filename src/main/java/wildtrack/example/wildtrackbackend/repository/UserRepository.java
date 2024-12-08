package wildtrack.example.wildtrackbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wildtrack.example.wildtrackbackend.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email); // Check if email already exists

    Optional<User> findByEmail(String email); // Find user by email

    Optional<User> findByIdNumber(String idNumber); // Find user by ID number

    List<User> findByRole(String role); // Fetch users by their role (e.g., "Student")

    List<User> findByQuarter(String quarter); // Fetch teachers by quarter

    List<User> findBySubject(String subject); // Fetch teachers by subject

    List<User> findByQuarterAndSubject(String quarter, String subject);
}
