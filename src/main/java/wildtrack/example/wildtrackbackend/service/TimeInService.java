package wildtrack.example.wildtrackbackend.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import wildtrack.example.wildtrackbackend.entity.LibraryHours;
import wildtrack.example.wildtrackbackend.repository.LibraryHoursRepository;
import wildtrack.example.wildtrackbackend.repository.TimeInRepository;

@Service
public class TimeInService {

    // Define the Philippines timezone (UTC+8)
    private static final ZoneId PHILIPPINES_ZONE = ZoneId.of("Asia/Manila");

    // Hardcoded library hours
    private static final int OPENING_HOUR = 8; // 8:00 AM
    private static final int CLOSING_HOUR = 17; // 5:00 PM

    @Autowired
    private LibraryHoursRepository libraryHoursRepository;

    @Autowired
    private TimeInRepository timeInRepository;

    public void recordTimeIn(String idNumber) {
        // Check current time in Philippines timezone
        LocalDateTime currentTime = LocalDateTime.now(PHILIPPINES_ZONE);

        // Create opening and closing times for today
        LocalDateTime openingTime = LocalDateTime.of(
                currentTime.getYear(),
                currentTime.getMonth(),
                currentTime.getDayOfMonth(),
                OPENING_HOUR, 0); // 8:00 AM

        LocalDateTime closingTime = LocalDateTime.of(
                currentTime.getYear(),
                currentTime.getMonth(),
                currentTime.getDayOfMonth(),
                CLOSING_HOUR, 0); // 5:00 PM

        // Format current time for error messages
        String formattedTime = currentTime.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"));

        // Check if current time is before opening time
        if (currentTime.isBefore(openingTime)) {
            throw new RuntimeException(
                    "Library time-in is only available starting at 8:00 AM. Current time: " + formattedTime);
        }

        // Check if current time is after closing time
        if (currentTime.isAfter(closingTime)) {
            throw new RuntimeException(
                    "Library time-in is closed after 5:00 PM. Please come back tomorrow at 8:00 AM. Current time: "
                            + formattedTime);
        }

        // Check for an open time-in record
        LibraryHours openTimeIn = libraryHoursRepository.findLatestByIdNumber(idNumber)
                .filter(libraryHours -> libraryHours.getTimeOut() == null)
                .orElse(null);

        if (openTimeIn != null) {
            throw new RuntimeException(
                    "You already have a time-in recorded without a time-out. Please record a time-out before clocking in again.");
        }

        // Create a new time-in record with the Philippine timezone
        LibraryHours libraryHours = new LibraryHours();
        libraryHours.setIdNumber(idNumber);
        libraryHours.setTimeIn(currentTime);
        libraryHoursRepository.save(libraryHours);
    }

    public long getActiveStudentsCount() {
        // Return count of students who have timed in but not timed out
        return timeInRepository.countByTimeOutIsNull();
    }
}