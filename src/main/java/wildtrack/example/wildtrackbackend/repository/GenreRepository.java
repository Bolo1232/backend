package wildtrack.example.wildtrackbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import wildtrack.example.wildtrackbackend.entity.Genre;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {
}
