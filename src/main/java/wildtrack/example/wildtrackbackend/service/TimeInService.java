package wildtrack.example.wildtrackbackend.service;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import wildtrack.example.wildtrackbackend.entity.LibraryHours;
import wildtrack.example.wildtrackbackend.repository.LibraryHoursRepository;
import wildtrack.example.wildtrackbackend.repository.TimeInRepository;

@Service
public class TimeInService {

    @Autowired
    private LibraryHoursRepository libraryHoursRepository;

    @Autowired
    private TimeInRepository timeInRepository;

    public void recordTimeIn(String idNumber) {
        // Check for an open time-in record
        LibraryHours openTimeIn = libraryHoursRepository.findLatestByIdNumber(idNumber)
                .filter(libraryHours -> libraryHours.getTimeOut() == null)
                .orElse(null);

        if (openTimeIn != null) {
            throw new RuntimeException(
                    "You already have a time-in recorded without a time-out. Please record a time-out before clocking in again.");
        }

        // Create a new time-in record
        LibraryHours libraryHours = new LibraryHours();
        libraryHours.setIdNumber(idNumber);
        libraryHours.setTimeIn(LocalDateTime.now());
        libraryHoursRepository.save(libraryHours);
    }

    public long getActiveStudentsCount() {
        // Return count of students who have timed in but not timed out
        return timeInRepository.countByTimeOutIsNull();
    }
}