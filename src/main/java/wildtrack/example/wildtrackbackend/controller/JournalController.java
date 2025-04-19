package wildtrack.example.wildtrackbackend.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import wildtrack.example.wildtrackbackend.entity.Book;
import wildtrack.example.wildtrackbackend.entity.Journal;
import wildtrack.example.wildtrackbackend.entity.User;
import wildtrack.example.wildtrackbackend.service.JournalService;
import wildtrack.example.wildtrackbackend.service.BookService;
import wildtrack.example.wildtrackbackend.service.UserService;

@RestController
@RequestMapping("/api/journals")
public class JournalController {

    @Autowired
    private JournalService journalService;

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    /**
     * Get all journals
     */
    @GetMapping("/all")
    public ResponseEntity<List<Journal>> getAllJournals() {
        try {
            List<Journal> journals = journalService.getAllJournals();
            return ResponseEntity.ok(journals);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    /**
     * Add a new journal entry
     */
    @PostMapping("/add")
    public ResponseEntity<?> addJournal(@RequestBody Journal journal) {
        try {
            Journal savedJournal = journalService.saveJournal(journal);
            return ResponseEntity.ok(savedJournal);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred while adding the journal entry."));
        }
    }

    /**
     * Get journals for a specific user
     */
    @GetMapping("/user/{idNumber}")
    public ResponseEntity<List<Journal>> getUserJournals(@PathVariable String idNumber) {
        try {
            List<Journal> userJournals = journalService.getJournalsByUser(idNumber);
            return ResponseEntity.ok(userJournals);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    /**
     * Add a book to a user's journal
     */
    @PutMapping("/{bookId}/add-to-journal/{idNumber}")
    public ResponseEntity<?> addBookToUserJournal(
            @PathVariable Long bookId,
            @PathVariable String idNumber,
            @RequestBody Map<String, Object> payload) {
        try {
            // Validate book exists
            Book book = bookService.getBookById(bookId)
                    .orElseThrow(() -> new RuntimeException("Book not found."));

            // Validate user exists
            User user = userService.getUserByIdNumber(idNumber);
            if (user == null) {
                throw new RuntimeException("User not found with idNumber: " + idNumber);
            }

            // Process payload
            LocalDate dateRead = LocalDate.parse((String) payload.get("dateRead"));
            int rating = Integer.parseInt(payload.get("rating").toString());
            String comment = payload.containsKey("comment") ? (String) payload.get("comment") : null;

            // Create the journal entry
            Journal journal = journalService.addBookToUserJournal(
                    bookId, idNumber, dateRead, rating, comment);

            return ResponseEntity.ok(Map.of(
                    "message", "Book added to user's journal successfully.",
                    "journalEntry", journal));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Add a computer usage entry to a user's journal
     */
    @PostMapping("/computer-usage/{idNumber}")
    public ResponseEntity<?> addComputerUsageToJournal(
            @PathVariable String idNumber,
            @RequestBody Map<String, Object> payload) {
        try {
            // Validate user exists
            User user = userService.getUserByIdNumber(idNumber);
            if (user == null) {
                throw new RuntimeException("User not found with idNumber: " + idNumber);
            }

            // Process payload
            String purpose = (String) payload.get("purpose");
            int rating = Integer.parseInt(payload.get("rating").toString());
            String comment = payload.containsKey("comment") ? (String) payload.get("comment") : null;

            // Create the journal entry
            Journal journal = journalService.addComputerUsageToJournal(
                    idNumber, purpose, rating, comment);

            return ResponseEntity.ok(Map.of(
                    "message", "Computer usage added to journal successfully.",
                    "journalEntry", journal));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Add a periodical entry to a user's journal
     */
    @PostMapping("/periodical/{idNumber}")
    public ResponseEntity<?> addPeriodicalToJournal(
            @PathVariable String idNumber,
            @RequestBody Map<String, Object> payload) {
        try {
            // Validate user exists
            User user = userService.getUserByIdNumber(idNumber);
            if (user == null) {
                throw new RuntimeException("User not found with idNumber: " + idNumber);
            }

            // Process payload
            String periodicalTitle = (String) payload.get("periodicalTitle");
            int rating = Integer.parseInt(payload.get("rating").toString());
            String comment = payload.containsKey("comment") ? (String) payload.get("comment") : null;

            // Create the journal entry
            Journal journal = journalService.addPeriodicalToJournal(
                    idNumber, periodicalTitle, rating, comment);

            return ResponseEntity.ok(Map.of(
                    "message", "Periodical reading added to journal successfully.",
                    "journalEntry", journal));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

}