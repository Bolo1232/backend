package wildtrack.example.wildtrackbackend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import wildtrack.example.wildtrackbackend.entity.SetLibraryHours;
import wildtrack.example.wildtrackbackend.repository.SetLibraryHoursRepository;

@Service
public class SetLibraryHoursService {
    
    @Autowired
    private SetLibraryHoursRepository repository;
    
    public SetLibraryHours setLibraryHours(SetLibraryHours setLibraryHours) {
        return repository.save(setLibraryHours);
    }
    
    public List<SetLibraryHours> getAllSetLibraryHours() {
        return repository.findAll();
    }
}