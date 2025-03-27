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
import wildtrack.example.wildtrackbackend.entity.User;
import wildtrack.example.wildtrackbackend.service.BookLogService;
import wildtrack.example.wildtrackbackend.service.BookService;
import wildtrack.example.wildtrackbackend.service.UserService;

@RestController
@RequestMapping("/api/booklog")

public class BookLogController {

    @Autowired
    private BookLogService bookLogService;
    @Autowired
    private BookService bookService;
    @Autowired
    private UserService userService;

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
            @RequestBody Map<String, Object> payload) {
        try {
            Book book = bookService.getBookById(bookId)
                    .orElseThrow(() -> new RuntimeException("Book not found."));

            User user = userService.getUserByIdNumber(idNumber);
            if (user == null) {
                throw new RuntimeException("User not found with idNumber: " + idNumber);
            }

            BookLog bookLog = new BookLog();
            bookLog.setTitle(book.getTitle());
            bookLog.setAuthor(book.getAuthor());
            bookLog.setAccessionNumber(book.getAccessionNumber());
            bookLog.setIdNumber(idNumber);
            bookLog.setBookTitle(book.getTitle());
            bookLog.setUserId(user.getId()); // Set user ID

            // Process payload
            LocalDate dateRead = LocalDate.parse((String) payload.get("dateRead"));
            int rating = Integer.parseInt(payload.get("rating").toString());
            String academicYear = (String) payload.get("academicYear");

            bookLog.setDateRead(dateRead);
            bookLog.setRating(rating);
            bookLog.setAcademicYear(academicYear);

            // Save the book log
            bookLogService.saveBookLog(bookLog);

            return ResponseEntity.ok(Map.of("message", "Book added to user's book log successfully."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

}
