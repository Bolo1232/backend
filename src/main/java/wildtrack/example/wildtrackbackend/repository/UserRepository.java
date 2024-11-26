package wildtrack.example.wildtrackbackend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import wildtrack.example.wildtrackbackend.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email); // Check if email already exists
    Optional<User> findByEmail(String email); // Find user by email
}
