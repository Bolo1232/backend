package wildtrack.example.wildtrackbackend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import wildtrack.example.wildtrackbackend.service.JournalService;
import wildtrack.example.wildtrackbackend.service.LibraryHoursService;

@RestController
@RequestMapping("/api/analytics")
public class StudentAnalyticsController {

    @Autowired
    private JournalService journalService;

    @Autowired
    private LibraryHoursService libraryHoursService;

    @GetMapping("/books-read/{idNumber}")
    public ResponseEntity<?> getNumberOfBooksReadByUser(
            @PathVariable String idNumber,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo) {
        try {
            // Removed academicYear parameter
            List<Map<String, Object>> booksReadData = journalService.getNumberOfBooksReadByUser(
                    idNumber, dateFrom, dateTo);
            return ResponseEntity.ok(booksReadData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred while fetching books read data."));
        }
    }

    @GetMapping("/minutes-spent/{idNumber}")
    public ResponseEntity<?> getTotalMinutesSpentByUser(
            @PathVariable String idNumber,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo) {
        try {
            List<Map<String, Object>> minutesSpentData = libraryHoursService.getTotalMinutesSpentByUser(
                    idNumber, dateFrom, dateTo);
            return ResponseEntity.ok(minutesSpentData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred while fetching minutes spent data."));
        }
    }

    // Global analytics endpoints
    @GetMapping("/most-read-books")
    public ResponseEntity<?> getMostReadBooks() {
        try {
            List<Map<String, Object>> mostReadBooks = journalService.getMostReadBooks();
            return ResponseEntity.ok(mostReadBooks);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred while fetching most read books."));
        }
    }

    @GetMapping("/highest-rated-books")
    public ResponseEntity<?> getHighestRatedBooks() {
        try {
            List<Map<String, Object>> highestRatedBooks = journalService.getHighestRatedBooks();
            return ResponseEntity.ok(highestRatedBooks);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred while fetching highest rated books."));
        }
    }

    // User-specific analytics endpoints
    @GetMapping("/most-read-books/{idNumber}")
    public ResponseEntity<?> getMostReadBooksByUser(
            @PathVariable String idNumber,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo) {
        try {
            // Removed academicYear parameter
            List<Map<String, Object>> mostReadBooks = journalService.getMostReadBooksByUser(
                    idNumber, dateFrom, dateTo);
            return ResponseEntity.ok(mostReadBooks);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred while fetching user's most read books."));
        }
    }

    @GetMapping("/highest-rated-books/{idNumber}")
    public ResponseEntity<?> getHighestRatedBooksByUser(
            @PathVariable String idNumber,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo) {
        try {
            // Removed academicYear parameter
            List<Map<String, Object>> highestRatedBooks = journalService.getHighestRatedBooksByUser(
                    idNumber, dateFrom, dateTo);
            return ResponseEntity.ok(highestRatedBooks);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred while fetching user's highest rated books."));
        }
    }
}