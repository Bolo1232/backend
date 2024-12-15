package wildtrack.example.wildtrackbackend.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import wildtrack.example.wildtrackbackend.entity.Book;
import wildtrack.example.wildtrackbackend.entity.BookLog;
import wildtrack.example.wildtrackbackend.service.BookLogService;
import wildtrack.example.wildtrackbackend.service.BookService;

@RestController
@RequestMapping("/api/booklog")
@CrossOrigin(origins = "http://localhost:5173") // Enable CORS for frontend
public class BookLogController {

    @Autowired
    private BookLogService bookLogService;
    @Autowired
    private BookService bookService;

    // Get all book logs
    @GetMapping("/all")
    public ResponseEntity<List<BookLog>> getAllBookLogs() {
        try {
            List<BookLog> bookLogs = bookLogService.getAllBookLogs();
            return ResponseEntity.ok(bookLogs);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    // Add a new book log
    @PostMapping("/add")
    public ResponseEntity<?> addBookLog(@RequestBody BookLog bookLog) {
        try {
            BookLog savedBookLog = bookLogService.saveBookLog(bookLog);
            return ResponseEntity.ok(savedBookLog);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while adding the book log.");
        }
    }

    @GetMapping("/user/{idNumber}")
    public ResponseEntity<List<BookLog>> getUserBookLogs(@PathVariable String idNumber) {
        try {
            List<BookLog> userBookLogs = bookLogService.getBookLogsByUser(idNumber);
            return ResponseEntity.ok(userBookLogs);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @PutMapping("/{bookId}/add-to-booklog/{idNumber}")
    public ResponseEntity<?> addBookToUserBookLog(
            @PathVariable Long bookId,
            @PathVariable String idNumber,
            @RequestBody Map<String, Object> payload // Accept additional fields
    ) {
        try {
            Book book = bookService.getBookById(bookId)
                    .orElseThrow(() -> new RuntimeException("Book not found."));

            BookLog bookLog = new BookLog();
            bookLog.setTitle(book.getTitle());
            bookLog.setAuthor(book.getAuthor());
            bookLog.setAccessionNumber(book.getAccessionNumber());
            bookLog.setIdNumber(idNumber);
            bookLog.setBookTitle(book.getTitle());
            bookLog.setUserId(Long.parseLong(idNumber));
            // Get additional fields from payload
            LocalDate dateRead = LocalDate.parse((String) payload.get("dateRead"));
            bookLog.setDateRead(dateRead);

            int rating = (int) payload.get("rating");
            bookLog.setRating(rating);

            String academicYear = (String) payload.get("academicYear");
            bookLog.setAcademicYear(academicYear);

            // Save the book log
            bookLogService.saveBookLog(bookLog);

            return ResponseEntity.ok(Map.of("message", "Book added to user's book log successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred."));
        }
    }

}
