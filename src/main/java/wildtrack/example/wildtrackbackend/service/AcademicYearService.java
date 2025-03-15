package wildtrack.example.wildtrackbackend.service;

import org.springframework.stereotype.Service;
import wildtrack.example.wildtrackbackend.entity.AcademicYear;
import wildtrack.example.wildtrackbackend.repository.AcademicYearRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AcademicYearService {

    private final AcademicYearRepository academicYearRepository;

    public AcademicYearService(AcademicYearRepository academicYearRepository) {
        this.academicYearRepository = academicYearRepository;
    }

    public List<AcademicYear> getActiveAcademicYears() {
        // Return academic years with status "Active" or null status (which defaults to
        // active)
        List<AcademicYear> allYears = academicYearRepository.findAll();
        return allYears.stream()
                .filter(year -> "Active".equals(year.getStatus()) || year.getStatus() == null)
                .collect(Collectors.toList());
    }

    public List<AcademicYear> getAllAcademicYears() {
        return academicYearRepository.findAll();
    }

    public AcademicYear saveAcademicYear(AcademicYear academicYear) {
        // Optional: Add validation logic here
        return academicYearRepository.save(academicYear);
    }

    public AcademicYear updateAcademicYear(Long id, AcademicYear academicYearDetails) {
        return academicYearRepository.findById(id)
                .map(existingAcademicYear -> {
                    // Update specific fields
                    existingAcademicYear.setStartYear(academicYearDetails.getStartYear());
                    existingAcademicYear.setEndYear(academicYearDetails.getEndYear());

                    // Update quarter details
                    existingAcademicYear.setFirstQuarter(academicYearDetails.getFirstQuarter());
                    existingAcademicYear.setSecondQuarter(academicYearDetails.getSecondQuarter());
                    existingAcademicYear.setThirdQuarter(academicYearDetails.getThirdQuarter());
                    existingAcademicYear.setFourthQuarter(academicYearDetails.getFourthQuarter());

                    return academicYearRepository.save(existingAcademicYear);
                })
                .orElseThrow(() -> new RuntimeException("Academic Year not found with id " + id));
    }

    public AcademicYear toggleArchiveStatus(Long id) {
        return academicYearRepository.findById(id)
                .map(academicYear -> {
                    // Toggle the status between 'Active' and 'Archived'
                    String currentStatus = academicYear.getStatus();
                    if (currentStatus == null || currentStatus.isEmpty()) {
                        currentStatus = "Active";
                    }

                    String newStatus = "Active".equals(currentStatus) ? "Archived" : "Active";
                    academicYear.setStatus(newStatus);
                    return academicYearRepository.save(academicYear);
                })
                .orElseThrow(() -> new RuntimeException("Academic Year not found with id " + id));
    }

    public boolean existsById(Long id) {
        return academicYearRepository.existsById(id);
    }

    public void deleteAcademicYearById(Long id) {
        if (!academicYearRepository.existsById(id)) {
            throw new RuntimeException("Academic Year not found with id " + id);
        }
        academicYearRepository.deleteById(id);
    }
}