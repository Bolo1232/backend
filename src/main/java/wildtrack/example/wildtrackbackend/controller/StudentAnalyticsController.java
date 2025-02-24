package wildtrack.example.wildtrackbackend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import wildtrack.example.wildtrackbackend.service.BookLogService;
import wildtrack.example.wildtrackbackend.service.LibraryHoursService;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "http://localhost:5173")
public class StudentAnalyticsController {

    @Autowired
    private BookLogService bookLogService;

    @Autowired
    private LibraryHoursService libraryHoursService;

    @GetMapping("/books-read/{idNumber}")
    public ResponseEntity<?> getNumberOfBooksReadByUser(@PathVariable String idNumber) {
        try {
            List<Map<String, Object>> booksReadData = bookLogService.getNumberOfBooksReadByUser(idNumber);
            return ResponseEntity.ok(booksReadData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred while fetching books read data."));
        }
    }

    @GetMapping("/minutes-spent/{idNumber}")
    public ResponseEntity<?> getTotalMinutesSpentByUser(@PathVariable String idNumber) {
        try {
            List<Map<String, Object>> minutesSpentData = libraryHoursService.getTotalMinutesSpentByUser(idNumber);
            return ResponseEntity.ok(minutesSpentData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred while fetching minutes spent data."));
        }
    }

    @GetMapping("/most-read-books")
    public ResponseEntity<?> getMostReadBooks() {
        try {
            List<Map<String, Object>> mostReadBooks = bookLogService.getMostReadBooks();
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
            List<Map<String, Object>> highestRatedBooks = bookLogService.getHighestRatedBooks();
            return ResponseEntity.ok(highestRatedBooks);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred while fetching highest rated books."));
        }
    }

}