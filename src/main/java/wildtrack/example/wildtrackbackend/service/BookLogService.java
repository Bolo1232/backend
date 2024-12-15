package wildtrack.example.wildtrackbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import wildtrack.example.wildtrackbackend.entity.BookLog;
import wildtrack.example.wildtrackbackend.repository.BookLogRepository;

import java.util.List;

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
}
