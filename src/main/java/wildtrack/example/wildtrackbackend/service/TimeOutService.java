package wildtrack.example.wildtrackbackend.service;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import wildtrack.example.wildtrackbackend.entity.LibraryHours;
import wildtrack.example.wildtrackbackend.repository.LibraryHoursRepository;

@Service
public class TimeOutService {

    @Autowired
    private LibraryHoursRepository libraryHoursRepository;

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
}