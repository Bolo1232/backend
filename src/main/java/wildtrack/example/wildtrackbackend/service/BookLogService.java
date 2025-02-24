package wildtrack.example.wildtrackbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import wildtrack.example.wildtrackbackend.entity.BookLog;
import wildtrack.example.wildtrackbackend.repository.BookLogRepository;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BookLogService {

    @Autowired
    private BookLogRepository bookLogRepository;

    public List<BookLog> getBookLogsByUser(String idNumber) {
        try {
            System.out.println("Querying book logs for user with ID: " + idNumber);
            List<BookLog> bookLogs = bookLogRepository.findByIdNumber(idNumber);
            System.out.println("Query result: " + bookLogs);
            return bookLogs;
        } catch (Exception e) {
            System.err.println("Error querying book logs for user with ID: " + idNumber);
            e.printStackTrace();
            throw new RuntimeException("Error fetching book logs", e);
        }
    }

    public BookLog saveBookLog(BookLog bookLog) {
        return bookLogRepository.save(bookLog);
    }

    public List<BookLog> getAllBookLogs() {
        return bookLogRepository.findAll();
    }

    public List<Map<String, Object>> getNumberOfBooksReadByUser(String idNumber) {
        List<BookLog> bookLogList = bookLogRepository.findByIdNumber(idNumber);

        Map<String, Integer> booksReadByMonth = new HashMap<>();

        for (BookLog bookLog : bookLogList) {
            String monthYear = bookLog.getDateRead().format(DateTimeFormatter.ofPattern("MMM yyyy"));
            booksReadByMonth.put(monthYear, booksReadByMonth.getOrDefault(monthYear, 0) + 1);
        }

        List<Map<String, Object>> booksReadData = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : booksReadByMonth.entrySet()) {
            Map<String, Object> dataPoint = new HashMap<>();
            dataPoint.put("month", entry.getKey());
            dataPoint.put("booksRead", entry.getValue());
            booksReadData.add(dataPoint);
        }

        return booksReadData;
    }

    public List<Map<String, Object>> getMostReadBooks() {
        List<Object[]> result = bookLogRepository.findMostReadBooks();

        List<Map<String, Object>> mostReadBooks = new ArrayList<>();
        for (Object[] row : result) {
            Map<String, Object> book = new HashMap<>();
            book.put("bookTitle", row[0]);
            book.put("timesRead", row[1]);
            mostReadBooks.add(book);
        }

        return mostReadBooks;
    }

    public List<Map<String, Object>> getHighestRatedBooks() {
        List<Object[]> result = bookLogRepository.findHighestRatedBooks();

        List<Map<String, Object>> highestRatedBooks = new ArrayList<>();
        for (Object[] row : result) {
            Map<String, Object> book = new HashMap<>();
            book.put("bookTitle", row[0]);
            book.put("averageRating", row[1]);
            highestRatedBooks.add(book);
        }

        return highestRatedBooks;
    }
}
