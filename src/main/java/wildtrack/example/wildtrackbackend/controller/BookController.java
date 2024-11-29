package wildtrack.example.wildtrackbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wildtrack.example.wildtrackbackend.entity.Book;
import wildtrack.example.wildtrackbackend.entity.LibraryHours;
import wildtrack.example.wildtrackbackend.service.BookService;
import wildtrack.example.wildtrackbackend.service.LibraryHoursService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "http://localhost:5173") // Enable CORS for frontend
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private LibraryHoursService libraryHoursService;

    @PostMapping("/add")
    public ResponseEntity<?> addBook(@RequestBody Book book) {
        try {
            if (bookService.existsByAccessionNumber(book.getAccessionNumber())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Accession number already exists.");
            }

            Book savedBook = bookService.saveBook(book);
            return ResponseEntity.ok(savedBook);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while adding the book.");
        }
    }

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
}
