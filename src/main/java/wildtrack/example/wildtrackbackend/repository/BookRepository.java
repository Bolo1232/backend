package wildtrack.example.wildtrackbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import wildtrack.example.wildtrackbackend.entity.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    boolean existsByAccessionNumber(String accessionNumber);
}
