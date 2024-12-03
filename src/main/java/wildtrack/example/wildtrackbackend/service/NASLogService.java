package wildtrack.example.wildtrackbackend.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import wildtrack.example.wildtrackbackend.entity.NASLog;
import wildtrack.example.wildtrackbackend.entity.User;
import wildtrack.example.wildtrackbackend.repository.NASLogRepository;
import wildtrack.example.wildtrackbackend.repository.UserRepository;

@Service
public class NASLogService {

    @Autowired
    private NASLogRepository nasLogRepository;

    @Autowired
    private UserRepository userRepository; // Add UserRepository to fetch user details

    public NASLog timeIn(String idNumber) {
        // Fetch the user by ID number
        User user = userRepository.findByIdNumber(idNumber)
                .orElseThrow(() -> new IllegalArgumentException("No user found with ID: " + idNumber));

        // Create a new log entry
        NASLog log = new NASLog();
        log.setIdNumber(idNumber);
        log.setName(user.getFirstName() + " " + user.getLastName()); // Set name from user
        log.setTimeIn(LocalDateTime.now());
        log.setStatus("Clocked In");

        return nasLogRepository.save(log);
    }

    public NASLog timeOut(String idNumber) {
        // Find the latest log for the user
        Optional<NASLog> latestLog = nasLogRepository.findTopByIdNumberOrderByIdDesc(idNumber);

        if (latestLog.isEmpty() || latestLog.get().getTimeOut() != null) {
            throw new IllegalStateException("No active Clock In record found.");
        }

        // Update the log entry
        NASLog log = latestLog.get();
        log.setTimeOut(LocalDateTime.now());
        log.setStatus("Clocked Out");

        return nasLogRepository.save(log);
    }

    public NASLog getLatestLog(String idNumber) {
        return nasLogRepository.findTopByIdNumberOrderByIdDesc(idNumber)
                .orElseThrow(() -> new IllegalArgumentException("No records found for ID: " + idNumber));
    }
}
