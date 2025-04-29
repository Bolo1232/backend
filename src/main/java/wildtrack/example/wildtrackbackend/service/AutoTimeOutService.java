package wildtrack.example.wildtrackbackend.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import wildtrack.example.wildtrackbackend.entity.LibraryHours;
import wildtrack.example.wildtrackbackend.repository.LibraryHoursRepository;

@Service
public class AutoTimeOutService {

    // Define the Philippines timezone (UTC+8)
    private static final ZoneId PHILIPPINES_ZONE = ZoneId.of("Asia/Manila");

    @Autowired
    private LibraryHoursRepository libraryHoursRepository;

    @Autowired
    private LibraryRequirementProgressService libraryRequirementProgressService;

    /**
     * Automatically time out all users at 5:00 PM Manila time
     */
    @Scheduled(cron = "0 0 17 * * *", zone = "Asia/Manila")
    @Transactional
    public void autoTimeOutAllUsers() {
        // Find all records where timeOut is null
        List<LibraryHours> openSessions = libraryHoursRepository.findByTimeOutIsNull();

        LocalDateTime closeTime = LocalDateTime.now(PHILIPPINES_ZONE);

        for (LibraryHours session : openSessions) {
            // Set time-out to current time for ALL sessions
            session.setTimeOut(closeTime);

            // Calculate minutes spent
            long minutes = java.time.Duration.between(session.getTimeIn(), session.getTimeOut()).toMinutes();
            session.setMinutesCounted((int) minutes);

            // If this session has a book assigned, mark it as counted
            // Otherwise, mark it as requiring book assignment
            if (session.getBookTitle() != null && !session.getBookTitle().trim().isEmpty()) {
                session.setIsCounted(true);

                // Only update library requirement progress for sessions with books
                LibraryHours savedRecord = libraryHoursRepository.save(session);
                libraryRequirementProgressService.recordLibraryTime(savedRecord.getId());
            } else {
                // For sessions without books, mark them specially
                session.setIsCounted(false);
                session.setRequiresBookAssignment(true); // Add this field to LibraryHours entity
                libraryHoursRepository.save(session);
            }
        }
    }
}