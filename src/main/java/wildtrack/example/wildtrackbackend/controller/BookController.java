package wildtrack.example.wildtrackbackend.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import wildtrack.example.wildtrackbackend.entity.Book;
import wildtrack.example.wildtrackbackend.entity.LibraryHours;
import wildtrack.example.wildtrackbackend.service.BookService;
import wildtrack.example.wildtrackbackend.service.LibraryHoursService;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "http://localhost:5173") // Enable CORS for frontend
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private LibraryHoursService libraryHoursService;

    // Add a new book
    @PostMapping("/add")
    public ResponseEntity<?> addBook(@RequestBody Book book) {
        try {
            if (bookService.existsByAccessionNumber(book.getAccessionNumber())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Accession number already exists.");
            }

            if (bookService.existsByIsbn(book.getIsbn())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ISBN already exists.");
            }

            Book savedBook = bookService.saveBook(book);
            return ResponseEntity.ok(savedBook);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while adding the book.");
        }
    }

    // Retrieve all books
    @GetMapping("/all")
    public ResponseEntity<List<Book>> getAllBooks() {
        try {
            List<Book> books = bookService.getAllBooks();
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Retrieve a specific book by its ID
    @GetMapping("/{id}")
public ResponseEntity<?> getBookById(@PathVariable Long id) {
    try {
        Optional<Book> bookOptional = bookService.getBookById(id);

        if (bookOptional.isPresent()) {
            return ResponseEntity.ok(bookOptional.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Book not found.");
        }
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred.");
    }
}


    // Assign a book to specific library hours
    @PutMapping("/{bookId}/assign-library-hours/{libraryHoursId}")
    public ResponseEntity<?> assignBookToLibraryHours(@PathVariable Long bookId, @PathVariable Long libraryHoursId) {
        try {
            Book book = bookService.getBookById(bookId)
                    .orElseThrow(() -> new RuntimeException("Book not found."));
            LibraryHours libraryHours = libraryHoursService.getLibraryHoursById(libraryHoursId)
                    .orElseThrow(() -> new RuntimeException("LibraryHours record not found."));

            libraryHours.setBookTitle(book.getTitle());
            libraryHoursService.saveLibraryHours(libraryHours);

            return ResponseEntity.ok(Map.of("message", "Book assigned to library hours successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred."));
        }
    }

    // Delete a book by its ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable Long id) {
        try {
            bookService.getBookById(id)
                    .orElseThrow(() -> new RuntimeException("Book not found."));
            bookService.deleteBook(id);
            return ResponseEntity.ok(Map.of("message", "Book deleted successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred."));
        }
    }
}
