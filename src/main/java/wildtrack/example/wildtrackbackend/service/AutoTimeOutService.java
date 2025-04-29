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
     * cron expression: second minute hour day-of-month month day-of-week
     * "0 0 17 * * *" = Run at 5:00 PM (17:00) every day
     */
    @Scheduled(cron = "0 0 17 * * *", zone = "Asia/Manila")
    @Transactional
    public void autoTimeOutAllUsers() {
        // Find all records where timeOut is null
        List<LibraryHours> openSessions = libraryHoursRepository.findByTimeOutIsNull();

        LocalDateTime closeTime = LocalDateTime.now(PHILIPPINES_ZONE);

        for (LibraryHours session : openSessions) {
            // Skip sessions that don't have a book assigned
            if (session.getBookTitle() == null || session.getBookTitle().trim().isEmpty()) {
                continue;
            }

            // Set time-out to 5:00 PM
            session.setTimeOut(closeTime);

            // Calculate minutes spent
            long minutes = java.time.Duration.between(session.getTimeIn(), session.getTimeOut()).toMinutes();
            session.setMinutesCounted((int) minutes);

            // Set isCounted to true to ensure it's included in reports
            session.setIsCounted(true);

            // Save the updated record
            LibraryHours savedRecord = libraryHoursRepository.save(session);

            // Update library requirement progress with this time
            libraryRequirementProgressService.recordLibraryTime(savedRecord.getId());
        }
    }
}