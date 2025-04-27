package wildtrack.example.wildtrackbackend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import wildtrack.example.wildtrackbackend.entity.Periodical;
import wildtrack.example.wildtrackbackend.repository.PeriodicalRepository;

@Service
public class PeriodicalService {

    @Autowired
    private PeriodicalRepository periodicalRepository;

    // Save a new periodical
    public Periodical savePeriodical(Periodical periodical) {
        return periodicalRepository.save(periodical);
    }

    // Retrieve all periodicals
    public List<Periodical> getAllPeriodicals() {
        return periodicalRepository.findAll();
    }

    // Check if a periodical exists by accession number
    public boolean existsByAccessionNumber(String accessionNumber) {
        return periodicalRepository.existsByAccessionNumber(accessionNumber);
    }

    // Retrieve a periodical by its ID
    public Optional<Periodical> getPeriodicalById(Long id) {
        return periodicalRepository.findById(id);
    }

    // Update an existing periodical
    public Optional<Periodical> updatePeriodical(Long id, Periodical updatedPeriodical) {
        return periodicalRepository.findById(id).map(existingPeriodical -> {
            existingPeriodical.setTitle(updatedPeriodical.getTitle());
            existingPeriodical.setAccessionNumber(updatedPeriodical.getAccessionNumber());
            existingPeriodical.setPublisher(updatedPeriodical.getPublisher());
            existingPeriodical.setPlaceOfPublication(updatedPeriodical.getPlaceOfPublication());
            existingPeriodical.setCopyright(updatedPeriodical.getCopyright());

            // Only update dateRegistered if it's provided
            if (updatedPeriodical.getDateRegistered() != null) {
                existingPeriodical.setDateRegistered(updatedPeriodical.getDateRegistered());
            }

            return periodicalRepository.save(existingPeriodical);
        });
    }

    // Delete a periodical by its ID
    public void deletePeriodical(Long id) {
        periodicalRepository.deleteById(id);
    }

    // Delete all periodicals
    public void deleteAllPeriodicals() {
        periodicalRepository.deleteAll();
    }
}