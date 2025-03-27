package wildtrack.example.wildtrackbackend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import wildtrack.example.wildtrackbackend.entity.Book;
import wildtrack.example.wildtrackbackend.entity.Genre;
import wildtrack.example.wildtrackbackend.repository.GenreRepository;

@Service
public class GenreService {

    @Autowired
    private GenreRepository genreRepository;
    
    @Autowired
    private BookService bookService;

    // Add a new genre
    public Genre addGenre(Genre genre) {
        // Ensure new genres are not archived by default
        genre.setArchived(false);
        return genreRepository.save(genre);
    }

    // Get all genres
    public List<Genre> getAllGenres() {
        return genreRepository.findAll();
    }

    // Get a genre by ID
    public Optional<Genre> getGenreById(Long id) {
        return genreRepository.findById(id);
    }

    // Update a genre with cascading changes to books
    @Transactional
    public Genre updateGenre(Long id, Genre updatedGenre) {
        Genre existingGenre = genreRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Genre not found with id: " + id));
            
        // If genre is archived, prevent updates
        if (existingGenre.isArchived()) {
            throw new RuntimeException("Cannot update archived genre. Unarchive it first.");
        }
            
        String oldGenreName = existingGenre.getGenre();
        String newGenreName = updatedGenre.getGenre();
        
        // Update the genre
        existingGenre.setGenre(newGenreName);
        existingGenre.setTitle(updatedGenre.getTitle());
        
        // Keep the archived status as is during normal updates
        // existingGenre.setArchived(existingGenre.isArchived());
        
        Genre savedGenre = genreRepository.save(existingGenre);
        
        // If genre name changed, update all books with this genre
        if (!oldGenreName.equals(newGenreName)) {
            updateBooksWithGenre(oldGenreName, newGenreName);
        }
        
        return savedGenre;
    }
    
    // Set archive status of a genre
    @Transactional
    public Genre setArchiveStatus(Long id, boolean archived) {
        Genre existingGenre = genreRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Genre not found with id: " + id));
            
        // Only update if the archive status is actually changing
        if (existingGenre.isArchived() != archived) {
            existingGenre.setArchived(archived);
            return genreRepository.save(existingGenre);
        }
        
        return existingGenre; // Return unchanged if status already matches request
    }

    // Helper method to update all books with a specific genre
    private void updateBooksWithGenre(String oldGenreName, String newGenreName) {
        List<Book> booksWithGenre = bookService.getBooksByGenre(oldGenreName);
        
        for (Book book : booksWithGenre) {
            book.setGenre(newGenreName);
            bookService.saveBook(book);
        }
    }

    // Delete a genre by ID
    public void deleteGenre(Long id) {
        genreRepository.deleteById(id);
    }
}
