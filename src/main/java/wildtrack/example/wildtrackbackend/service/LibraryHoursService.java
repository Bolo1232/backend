package wildtrack.example.wildtrackbackend.service;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import wildtrack.example.wildtrackbackend.entity.LibraryHours;
import wildtrack.example.wildtrackbackend.entity.User;
import wildtrack.example.wildtrackbackend.repository.LibraryHoursRepository;
import wildtrack.example.wildtrackbackend.repository.UserRepository;
import wildtrack.example.wildtrackbackend.dto.StudentLibrarySummary;
import wildtrack.example.wildtrackbackend.entity.User;


import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LibraryHoursService {

    @Autowired
    private LibraryHoursRepository libraryHoursRepository;
     private UserService userService;

    @Autowired

    private UserRepository userRepository;

    public List<StudentLibrarySummary> getLibraryHoursSummary() {
        List<User> users = userRepository.findAll();
        List<StudentLibrarySummary> summaries = new ArrayList<>();

        // Formatter for 12-hour time with AM/PM
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a");

        for (User user : users) {
            List<LibraryHours> libraryHoursList = libraryHoursRepository.findByIdNumber(user.getIdNumber());

            if (!libraryHoursList.isEmpty()) {
                LibraryHours latestRecord = libraryHoursList.get(libraryHoursList.size() - 1);

                // Calculate total minutes
                long totalMinutes = libraryHoursList.stream()
                        .mapToLong(hours -> {
                            if (hours.getTimeIn() != null && hours.getTimeOut() != null) {
                                return Duration.between(hours.getTimeIn(), hours.getTimeOut()).toMinutes();
                            }
                            return 0;
                        }).sum();

                // Create summary
                StudentLibrarySummary summary = new StudentLibrarySummary();
                summary.setIdNumber(user.getIdNumber());
                summary.setFirstName(user.getFirstName());
                summary.setLastName(user.getLastName());
                summary.setLatestLibraryHourDate(latestRecord.getTimeIn().toLocalDate().toString());
                summary.setLatestTimeIn(latestRecord.getTimeIn().format(timeFormatter)); // Format as 12-hour time
                summary.setLatestTimeOut(latestRecord.getTimeOut() != null
                        ? latestRecord.getTimeOut().format(timeFormatter)
                        : "N/A");
                summary.setTotalMinutes(String.valueOf(totalMinutes));

                summaries.add(summary);
            }
        }

        return summaries;
    }


    // Record a time-in entry
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

    // Record a time-out entry
    public void recordTimeOut(String idNumber) {
        LibraryHours libraryHours = libraryHoursRepository.findLatestByIdNumber(idNumber)
                .orElseThrow(() -> new RuntimeException("No open time-in record found for this student."));

        if (libraryHours.getTimeOut() != null) {
            throw new RuntimeException("Time-out has already been recorded for the latest time-in.");
        }

        // Record time-out for the most recent time-in
        libraryHours.setTimeOut(LocalDateTime.now());
        libraryHoursRepository.save(libraryHours);
    }

    // Fetch all library hours
    public List<LibraryHours> getAllLibraryHours() {
        return libraryHoursRepository.findAll();
    }

    // Fetch library hours by user ID number
    public List<LibraryHours> getLibraryHoursByIdNumber(String idNumber) {
        return libraryHoursRepository.findByIdNumber(idNumber);
    }

    // Fetch all library hours with user details
    public List<Map<String, Object>> getAllLibraryHoursWithUserDetails() {
        List<LibraryHours> libraryHoursList = libraryHoursRepository.findAll();
        List<Map<String, Object>> response = new ArrayList<>();

        for (LibraryHours libraryHours : libraryHoursList) {
            User user = userService.findByIdNumber(libraryHours.getIdNumber());
            Map<String, Object> entry = new HashMap<>();
            entry.put("id", libraryHours.getId());
            entry.put("idNumber", libraryHours.getIdNumber());
            entry.put("firstName", user != null ? user.getFirstName() : "Unknown");
            entry.put("lastName", user != null ? user.getLastName() : "Unknown");
            entry.put("timeIn", libraryHours.getTimeIn());
            entry.put("timeOut", libraryHours.getTimeOut());
            entry.put("status", libraryHours.getTimeOut() != null ? "Present" : "Incomplete");
            response.add(entry);
        }
        return response;
    }

    // Fetch a specific library hours record by ID
    public Optional<LibraryHours> getLibraryHoursById(Long id) {
        return libraryHoursRepository.findById(id);
    }

    // Save or update a library hours record
    public LibraryHours saveLibraryHours(LibraryHours libraryHours) {
        return libraryHoursRepository.save(libraryHours);
    }

    // Calculate Active Library Hours Participants (Average minutes spent by all
    // students)
    public double calculateAverageMinutes() {
        List<LibraryHours> libraryHours = libraryHoursRepository.findAll();

        // Calculate minutes for each record
        List<Long> durations = libraryHours.stream()
                .filter(lh -> lh.getTimeIn() != null && lh.getTimeOut() != null)
                .map(lh -> java.time.Duration.between(lh.getTimeIn(), lh.getTimeOut()).toMinutes())
                .collect(Collectors.toList());

        // Calculate average
        return durations.isEmpty() ? 0 : durations.stream().mapToLong(Long::longValue).average().orElse(0);
    }

    // Calculate Accession Usage Frequency
    public Map<String, Long> calculateAccessionUsageFrequency() {
        List<LibraryHours> libraryHours = libraryHoursRepository.findAll();

        // Count occurrences of each book title
        return libraryHours.stream()
                .filter(lh -> lh.getBookTitle() != null && !lh.getBookTitle().isEmpty())
                .collect(Collectors.groupingBy(LibraryHours::getBookTitle, Collectors.counting()));
    }

    // Fetch all library hours

}
