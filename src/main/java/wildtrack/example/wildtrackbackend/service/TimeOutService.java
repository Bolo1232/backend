package wildtrack.example.wildtrackbackend.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import wildtrack.example.wildtrackbackend.entity.LibraryHours;
import wildtrack.example.wildtrackbackend.repository.LibraryHoursRepository;

@Service
public class TimeOutService {

    @Autowired
    private LibraryHoursRepository libraryHoursRepository;

    @Autowired
    private LibraryRequirementProgressService libraryRequirementProgressService;

    /**
     * Record time-out for a student, ensuring they have a book assigned
     */
    @Transactional
    public LibraryHours recordTimeOut(String idNumber) {
        // Find the latest time-in record without a time-out
        Optional<LibraryHours> openTimeInOpt = libraryHoursRepository
                .findByIdNumberAndTimeOutIsNullOrderByTimeInDesc(idNumber)
                .stream()
                .findFirst();

        if (openTimeInOpt.isEmpty()) {
            throw new RuntimeException("No open time-in record found. Please time-in first.");
        }

        LibraryHours openTimeIn = openTimeInOpt.get();

        // Check if a book has been assigned to this library session
        if (openTimeIn.getBookTitle() == null || openTimeIn.getBookTitle().trim().isEmpty()) {
            throw new RuntimeException("Please assign a book to your library session before timing out.");
        }

        // Record time-out
        openTimeIn.setTimeOut(LocalDateTime.now());

        // Calculate minutes spent
        long minutes = java.time.Duration.between(openTimeIn.getTimeIn(), openTimeIn.getTimeOut()).toMinutes();
        openTimeIn.setMinutesCounted((int) minutes);

        // Set isCounted to true to ensure it's included in reports
        openTimeIn.setIsCounted(true);

        // Save the updated record
        LibraryHours savedRecord = libraryHoursRepository.save(openTimeIn);

        // Update library requirement progress with this time
        libraryRequirementProgressService.recordLibraryTime(savedRecord.getId());

        return savedRecord;
    }

    /**
     * Record time-out with subject for a student, ensuring they have a book
     * assigned
     */
    @Transactional
    public LibraryHours recordTimeOutWithSubject(String idNumber, String subject) {
        // Find the latest time-in record without a time-out
        Optional<LibraryHours> openTimeInOpt = libraryHoursRepository
                .findByIdNumberAndTimeOutIsNullOrderByTimeInDesc(idNumber)
                .stream()
                .findFirst();

        if (openTimeInOpt.isEmpty()) {
            throw new RuntimeException("No open time-in record found. Please time-in first.");
        }

        LibraryHours openTimeIn = openTimeInOpt.get();

        // Check if a book has been assigned to this library session
        if (openTimeIn.getBookTitle() == null || openTimeIn.getBookTitle().trim().isEmpty()) {
            throw new RuntimeException("Please assign a book to your library session before timing out.");
        }

        // Record time-out and subject
        openTimeIn.setTimeOut(LocalDateTime.now());
        openTimeIn.setSubject(subject);

        // Calculate minutes spent
        long minutes = java.time.Duration.between(openTimeIn.getTimeIn(), openTimeIn.getTimeOut()).toMinutes();
        openTimeIn.setMinutesCounted((int) minutes);

        // Set isCounted to true to ensure it's included in reports
        openTimeIn.setIsCounted(true);

        // Save the updated record
        LibraryHours savedRecord = libraryHoursRepository.save(openTimeIn);

        // Update library requirement progress with this time
        libraryRequirementProgressService.recordLibraryTime(savedRecord.getId());

        return savedRecord;
    }
}