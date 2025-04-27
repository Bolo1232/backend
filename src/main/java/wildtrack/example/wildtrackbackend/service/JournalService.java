package wildtrack.example.wildtrackbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import wildtrack.example.wildtrackbackend.entity.Book;
import wildtrack.example.wildtrackbackend.entity.Journal;
import wildtrack.example.wildtrackbackend.entity.User;
import wildtrack.example.wildtrackbackend.repository.BookRepository;
import wildtrack.example.wildtrackbackend.repository.JournalRepository;
import wildtrack.example.wildtrackbackend.repository.UserRepository;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JournalService {

    @Autowired
    private JournalRepository journalRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    /**
     * Get all journals for a specific user
     */
    public List<Journal> getJournalsByUser(String idNumber) {
        try {
            System.out.println("Querying journals for user with ID: " + idNumber);
            List<Journal> journals = journalRepository.findByIdNumber(idNumber);
            System.out.println("Query result: " + journals);
            return journals;
        } catch (Exception e) {
            System.err.println("Error querying journals for user with ID: " + idNumber);
            e.printStackTrace();
            throw new RuntimeException("Error fetching journals", e);
        }
    }

    /**
     * Save a journal entry with auto-incrementing entry number
     */
    @Transactional
    public Journal saveJournal(Journal journal) {
        // If no entry number is provided, generate one
        if (journal.getEntryNo() == null || journal.getEntryNo().isEmpty()) {
            String nextEntryNo = generateNextEntryNumber();
            journal.setEntryNo(nextEntryNo);
        }

        // Set date logged if not provided
        if (journal.getDateLogged() == null) {
            journal.setDateLogged(LocalDate.now());
        }

        return journalRepository.save(journal);
    }

    /**
     * Get all journals
     */
    public List<Journal> getAllJournals() {
        return journalRepository.findAll();
    }

    /**
     * Get number of books read by user with optional filters
     */
    public List<Map<String, Object>> getNumberOfBooksReadByUser(
            String idNumber, String dateFrom, String dateTo, String academicYear) {

        // Convert academicYear to date range if provided
        if (academicYear != null && !academicYear.isEmpty()) {
            try {
                String[] years = academicYear.split("-");
                int startYear = Integer.parseInt(years[0]);
                int endYear = Integer.parseInt(years[1]);

                // Assuming academic year starts in August and ends in July
                dateFrom = LocalDate.of(startYear, Month.AUGUST, 1).toString(); // August 1st of start year
                dateTo = LocalDate.of(endYear, Month.JULY, 31).toString(); // July 31st of end year
            } catch (Exception e) {
                // Log error and continue with existing date parameters
                System.err.println("Error parsing academic year: " + academicYear);
            }
        }

        List<Journal> journalList = journalRepository.findByIdNumberAndActivity(idNumber, "Read Book");

        // Apply filters if provided
        if (dateFrom != null || dateTo != null) {
            LocalDate fromDate = dateFrom != null ? LocalDate.parse(dateFrom) : null;
            LocalDate toDate = dateTo != null ? LocalDate.parse(dateTo) : null;

            journalList = journalList.stream()
                    .filter(log -> {
                        boolean includeLog = true;

                        // Filter by date range
                        if (fromDate != null && log.getDateRead().isBefore(fromDate)) {
                            includeLog = false;
                        }
                        if (toDate != null && log.getDateRead().isAfter(toDate)) {
                            includeLog = false;
                        }

                        return includeLog;
                    })
                    .collect(Collectors.toList());
        }

        Map<String, Integer> booksReadByMonth = new HashMap<>();

        for (Journal journal : journalList) {
            String monthYear = journal.getDateRead().format(DateTimeFormatter.ofPattern("MMM yyyy"));
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

    /**
     * Add a book to a user's journal
     */
    @Transactional
    public Journal addBookToUserJournal(Long bookId, String idNumber, LocalDate dateRead,
            int rating, String comment) {
        // Validate user exists
        User user = userRepository.findByIdNumber(idNumber)
                .orElseThrow(() -> new RuntimeException("User not found with idNumber: " + idNumber));

        // Validate book exists
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));

        // Create new journal entry
        Journal journal = new Journal();
        journal.setEntryNo(generateNextEntryNumber());
        journal.setIdNumber(idNumber);
        journal.setUserId(user.getId());
        journal.setDateRead(dateRead);
        journal.setRating(rating);
        journal.setComment(comment);
        journal.setDateLogged(LocalDate.now());

        // Set activity and details for the journal
        journal.setActivity("Read Book");
        journal.setDetails(book.getTitle());

        return journalRepository.save(journal);
    }

    /**
     * Add a computer usage entry to a user's journal
     */
    @Transactional
    public Journal addComputerUsageToJournal(String idNumber, String purpose,
            int rating, String comment) {
        // Validate user exists
        User user = userRepository.findByIdNumber(idNumber)
                .orElseThrow(() -> new RuntimeException("User not found with idNumber: " + idNumber));

        // Create new journal entry
        Journal journal = new Journal();
        journal.setEntryNo(generateNextEntryNumber());
        journal.setIdNumber(idNumber);
        journal.setUserId(user.getId());
        journal.setDateRead(LocalDate.now());
        journal.setRating(rating);
        journal.setComment(comment);
        journal.setDateLogged(LocalDate.now());

        // Set activity and details for the journal
        journal.setActivity("Used Computer");
        journal.setDetails(purpose);

        return journalRepository.save(journal);
    }

    /**
     * Add a periodical reading entry to a user's journal
     */
    @Transactional
    public Journal addPeriodicalToJournal(String idNumber, String periodicalTitle,
            int rating, String comment) {
        // Validate user exists
        User user = userRepository.findByIdNumber(idNumber)
                .orElseThrow(() -> new RuntimeException("User not found with idNumber: " + idNumber));

        // Create new journal entry
        Journal journal = new Journal();
        journal.setEntryNo(generateNextEntryNumber());
        journal.setIdNumber(idNumber);
        journal.setUserId(user.getId());
        journal.setDateRead(LocalDate.now());
        journal.setRating(rating);
        journal.setComment(comment);
        journal.setDateLogged(LocalDate.now());

        // Set activity and details for the journal
        journal.setActivity("Read Periodical");
        journal.setDetails(periodicalTitle);

        return journalRepository.save(journal);
    }

    // Global analytics methods
    public List<Map<String, Object>> getMostReadBooks() {
        List<Object[]> result = journalRepository.findMostReadBooks();

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
        List<Object[]> result = journalRepository.findHighestRatedBooks();

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

        // Convert academicYear to date range if provided
        if (academicYear != null && !academicYear.isEmpty()) {
            try {
                String[] years = academicYear.split("-");
                int startYear = Integer.parseInt(years[0]);
                int endYear = Integer.parseInt(years[1]);

                // Assuming academic year starts in August and ends in July
                dateFrom = LocalDate.of(startYear, Month.AUGUST, 1).toString(); // August 1st of start year
                dateTo = LocalDate.of(endYear, Month.JULY, 31).toString(); // July 31st of end year
            } catch (Exception e) {
                // Log error and continue with existing date parameters
                System.err.println("Error parsing academic year: " + academicYear);
            }
        }

        List<Journal> userJournals = journalRepository.findByIdNumberAndActivity(idNumber, "Read Book");

        // Apply filters if provided
        if (dateFrom != null || dateTo != null) {
            LocalDate fromDate = dateFrom != null ? LocalDate.parse(dateFrom) : null;
            LocalDate toDate = dateTo != null ? LocalDate.parse(dateTo) : null;

            userJournals = userJournals.stream()
                    .filter(log -> {
                        boolean includeLog = true;

                        // Filter by date range
                        if (fromDate != null && log.getDateRead().isBefore(fromDate)) {
                            includeLog = false;
                        }
                        if (toDate != null && log.getDateRead().isAfter(toDate)) {
                            includeLog = false;
                        }

                        return includeLog;
                    })
                    .collect(Collectors.toList());
        }

        // Count occurrences of each book title
        Map<String, Integer> bookCounts = new HashMap<>();
        for (Journal log : userJournals) {
            String bookTitle = log.getDetails(); // Use details field instead of bookTitle
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

        // Convert academicYear to date range if provided
        if (academicYear != null && !academicYear.isEmpty()) {
            try {
                String[] years = academicYear.split("-");
                int startYear = Integer.parseInt(years[0]);
                int endYear = Integer.parseInt(years[1]);

                // Assuming academic year starts in August and ends in July
                dateFrom = LocalDate.of(startYear, Month.AUGUST, 1).toString(); // August 1st of start year
                dateTo = LocalDate.of(endYear, Month.JULY, 31).toString(); // July 31st of end year
            } catch (Exception e) {
                // Log error and continue with existing date parameters
                System.err.println("Error parsing academic year: " + academicYear);
            }
        }

        List<Journal> userJournals = journalRepository.findByIdNumberAndActivity(idNumber, "Read Book");

        // Apply filters if provided
        if (dateFrom != null || dateTo != null) {
            LocalDate fromDate = dateFrom != null ? LocalDate.parse(dateFrom) : null;
            LocalDate toDate = dateTo != null ? LocalDate.parse(dateTo) : null;

            userJournals = userJournals.stream()
                    .filter(log -> {
                        boolean includeLog = true;

                        // Filter by date range
                        if (fromDate != null && log.getDateRead().isBefore(fromDate)) {
                            includeLog = false;
                        }
                        if (toDate != null && log.getDateRead().isAfter(toDate)) {
                            includeLog = false;
                        }

                        return includeLog;
                    })
                    .collect(Collectors.toList());
        }

        // Only include logs with ratings
        userJournals = userJournals.stream()
                .filter(log -> log.getRating() > 0)
                .collect(Collectors.toList());

        // Group by book title and calculate average rating
        Map<String, List<Integer>> ratingsByBook = new HashMap<>();
        for (Journal log : userJournals) {
            String bookTitle = log.getDetails(); // Use details field instead of bookTitle
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

    /**
     * Generate the next sequential entry number
     * This finds the highest entry number in the database and increments it by 1
     */
    private String generateNextEntryNumber() {
        // Get the journal with the highest entry number
        Optional<Integer> maxEntryNo = journalRepository.findAll().stream()
                .map(journal -> {
                    try {
                        return Integer.parseInt(journal.getEntryNo());
                    } catch (NumberFormatException e) {
                        return 0; // If not a valid number, treat as 0
                    }
                })
                .max(Integer::compareTo);

        // If there are no journals yet or no valid entry numbers, start with 1
        int nextEntryNo = maxEntryNo.orElse(0) + 1;

        return String.valueOf(nextEntryNo);
    }
}