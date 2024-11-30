package wildtrack.example.wildtrackbackend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import wildtrack.example.wildtrackbackend.entity.LibraryHours;
import wildtrack.example.wildtrackbackend.entity.User;
import wildtrack.example.wildtrackbackend.repository.LibraryHoursRepository;

@Service
public class LibraryHoursService {

    @Autowired
    private LibraryHoursRepository libraryHoursRepository;

    @Autowired
    private UserService userService;

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
}
