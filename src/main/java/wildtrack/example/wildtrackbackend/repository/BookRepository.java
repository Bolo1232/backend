package wildtrack.example.wildtrackbackend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import wildtrack.example.wildtrackbackend.entity.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    boolean existsByAccessionNumber(String accessionNumber);
    boolean existsByIsbn(String isbn);
    List<Book> findByGenre(String genre); // Custom query to fetch books by genre
}
