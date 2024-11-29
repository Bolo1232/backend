package wildtrack.example.wildtrackbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wildtrack.example.wildtrackbackend.entity.LibraryHours;
import wildtrack.example.wildtrackbackend.repository.LibraryHoursRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LibraryHoursService {

    @Autowired
    private LibraryHoursRepository libraryHoursRepository;

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

    // Fetch a specific library hours record by ID
    public Optional<LibraryHours> getLibraryHoursById(Long id) {
        return libraryHoursRepository.findById(id);
    }

    // Save or update a library hours record
    public LibraryHours saveLibraryHours(LibraryHours libraryHours) {
        return libraryHoursRepository.save(libraryHours);
    }
}
