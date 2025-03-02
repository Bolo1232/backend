package wildtrack.example.wildtrackbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import wildtrack.example.wildtrackbackend.entity.BookLog;
import wildtrack.example.wildtrackbackend.repository.BookLogRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public List<Map<String, Object>> getNumberOfBooksReadByUser(
            String idNumber, String dateFrom, String dateTo, String academicYear) {

        List<BookLog> bookLogList = bookLogRepository.findByIdNumber(idNumber);

        // Apply filters if provided
        if (dateFrom != null || dateTo != null || academicYear != null) {
            LocalDate fromDate = dateFrom != null ? LocalDate.parse(dateFrom) : null;
            LocalDate toDate = dateTo != null ? LocalDate.parse(dateTo) : null;

            bookLogList = bookLogList.stream()
                    .filter(log -> {
                        boolean includeLog = true;

                        // Filter by date range
                        if (fromDate != null && log.getDateRead().isBefore(fromDate)) {
                            includeLog = false;
                        }
                        if (toDate != null && log.getDateRead().isAfter(toDate)) {
                            includeLog = false;
                        }

                        // Filter by academic year if provided
                        if (academicYear != null && !academicYear.isEmpty() &&
                                !academicYear.equals(log.getAcademicYear())) {
                            includeLog = false;
                        }

                        return includeLog;
                    })
                    .collect(Collectors.toList());
        }

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

    // Global analytics methods
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

    // User-specific analytics methods
    public List<Map<String, Object>> getMostReadBooksByUser(
            String idNumber, String dateFrom, String dateTo, String academicYear) {

        List<BookLog> userBookLogs = bookLogRepository.findByIdNumber(idNumber);

        // Apply filters if provided
        if (dateFrom != null || dateTo != null || academicYear != null) {
            LocalDate fromDate = dateFrom != null ? LocalDate.parse(dateFrom) : null;
            LocalDate toDate = dateTo != null ? LocalDate.parse(dateTo) : null;

            userBookLogs = userBookLogs.stream()
                    .filter(log -> {
                        boolean includeLog = true;

                        // Filter by date range
                        if (fromDate != null && log.getDateRead().isBefore(fromDate)) {
                            includeLog = false;
                        }
                        if (toDate != null && log.getDateRead().isAfter(toDate)) {
                            includeLog = false;
                        }

                        // Filter by academic year if provided
                        if (academicYear != null && !academicYear.isEmpty() &&
                                !academicYear.equals(log.getAcademicYear())) {
                            includeLog = false;
                        }

                        return includeLog;
                    })
                    .collect(Collectors.toList());
        }

        // Count occurrences of each book title
        Map<String, Integer> bookCounts = new HashMap<>();
        for (BookLog log : userBookLogs) {
            String bookTitle = log.getBookTitle();
            bookCounts.put(bookTitle, bookCounts.getOrDefault(bookTitle, 0) + 1);
        }

        // Convert to list of maps for the response
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : bookCounts.entrySet()) {
            Map<String, Object> bookData = new HashMap<>();
            bookData.put("bookTitle", entry.getKey());
            bookData.put("timesRead", entry.getValue());
            result.add(bookData);
        }

        // Sort by times read (descending)
        result.sort((a, b) -> ((Integer) b.get("timesRead")).compareTo((Integer) a.get("timesRead")));

        // Limit to top 10 books
        if (result.size() > 10) {
            result = result.subList(0, 10);
        }

        return result;
    }

    public List<Map<String, Object>> getHighestRatedBooksByUser(
            String idNumber, String dateFrom, String dateTo, String academicYear) {

        List<BookLog> userBookLogs = bookLogRepository.findByIdNumber(idNumber);

        // Apply filters if provided
        if (dateFrom != null || dateTo != null || academicYear != null) {
            LocalDate fromDate = dateFrom != null ? LocalDate.parse(dateFrom) : null;
            LocalDate toDate = dateTo != null ? LocalDate.parse(dateTo) : null;

            userBookLogs = userBookLogs.stream()
                    .filter(log -> {
                        boolean includeLog = true;

                        // Filter by date range
                        if (fromDate != null && log.getDateRead().isBefore(fromDate)) {
                            includeLog = false;
                        }
                        if (toDate != null && log.getDateRead().isAfter(toDate)) {
                            includeLog = false;
                        }

                        // Filter by academic year if provided
                        if (academicYear != null && !academicYear.isEmpty() &&
                                !academicYear.equals(log.getAcademicYear())) {
                            includeLog = false;
                        }

                        return includeLog;
                    })
                    .collect(Collectors.toList());
        }

        // Only include logs with ratings
        userBookLogs = userBookLogs.stream()
                .filter(log -> log.getRating() > 0)
                .collect(Collectors.toList());

        // Group by book title and calculate average rating
        Map<String, List<Integer>> ratingsByBook = new HashMap<>();
        for (BookLog log : userBookLogs) {
            String bookTitle = log.getBookTitle();
            ratingsByBook.computeIfAbsent(bookTitle, k -> new ArrayList<>())
                    .add(log.getRating());
        }

        // Calculate average ratings
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<Integer>> entry : ratingsByBook.entrySet()) {
            String bookTitle = entry.getKey();
            List<Integer> ratings = entry.getValue();

            // Calculate average rating
            double averageRating = ratings.stream()
                    .mapToDouble(Integer::doubleValue)
                    .average()
                    .orElse(0.0);

            Map<String, Object> bookData = new HashMap<>();
            bookData.put("bookTitle", bookTitle);
            bookData.put("averageRating", averageRating);
            bookData.put("ratingsCount", ratings.size());

            result.add(bookData);
        }

        // Sort by average rating (descending)
        result.sort((a, b) -> ((Double) b.get("averageRating")).compareTo((Double) a.get("averageRating")));

        // Limit to top 10 books
        if (result.size() > 10) {
            result = result.subList(0, 10);
        }

        return result;
    }
}