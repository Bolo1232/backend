package wildtrack.example.wildtrackbackend.service;

import java.time.LocalDateTime;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import wildtrack.example.wildtrackbackend.entity.LibraryHours;
import wildtrack.example.wildtrackbackend.repository.LibraryHoursRepository;

@Service
public class TimeOutService {
    private static final Logger logger = Logger.getLogger(TimeOutService.class.getName());

    @Autowired
    private LibraryHoursRepository libraryHoursRepository;

    @Autowired
    private LibraryRequirementProgressService progressService;

    /**
     * Record time-out for a student
     * This maintains compatibility with the original method signature
     */
    @Transactional
    public LibraryHours recordTimeOut(String idNumber) {
        logger.info("Recording time-out for student: " + idNumber);

        LibraryHours libraryHours = libraryHoursRepository.findLatestByIdNumber(idNumber)
                .orElseThrow(() -> new RuntimeException("No open time-in record found for this student."));

        if (libraryHours.getTimeOut() != null) {
            throw new RuntimeException("Time-out has already been recorded for the latest time-in.");
        }

        // Record time-out for the most recent time-in
        libraryHours.setTimeOut(LocalDateTime.now());
        LibraryHours savedHours = libraryHoursRepository.save(libraryHours);

        // Track the time toward requirements progress
        try {
            progressService.recordLibraryTime(savedHours.getId());
            logger.info("Successfully recorded library time for requirements progress");
        } catch (Exception e) {
            logger.warning("Error recording library time for requirements: " + e.getMessage());
        }

        return savedHours; // Return the saved hours
    }

    /**
     * Record time-out with a specific subject
     */
    @Transactional
    public LibraryHours recordTimeOutWithSubject(String idNumber, String subject) {
        logger.info("Recording time-out with subject for student: " + idNumber);

        LibraryHours libraryHours = libraryHoursRepository.findLatestByIdNumber(idNumber)
                .orElseThrow(() -> new RuntimeException("No open time-in record found for this student."));

        if (libraryHours.getTimeOut() != null) {
            throw new RuntimeException("Time-out has already been recorded for the latest time-in.");
        }

        // Set the subject if provided
        if (subject != null && !subject.isEmpty()) {
            libraryHours.setSubject(subject);
        }

        // Record time-out for the most recent time-in
        libraryHours.setTimeOut(LocalDateTime.now());
        LibraryHours savedHours = libraryHoursRepository.save(libraryHours);

        // Track the time toward requirements progress
        try {
            progressService.recordLibraryTime(savedHours.getId());
            logger.info("Successfully recorded library time for requirements progress");
        } catch (Exception e) {
            logger.warning("Error recording library time for requirements: " + e.getMessage());
            // We don't rethrow the exception to avoid disrupting existing flows
        }

        return savedHours;
    }
}