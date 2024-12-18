package wildtrack.example.wildtrackbackend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import wildtrack.example.wildtrackbackend.entity.Genre;
import wildtrack.example.wildtrackbackend.repository.GenreRepository;

@Service
public class GenreService {

    @Autowired
    private GenreRepository genreRepository;

    // Add a new genre
    public Genre addGenre(Genre genre) {
        return genreRepository.save(genre);
    }

    // Get all genres
    public List<Genre> getAllGenres() {
        return genreRepository.findAll();
    }

    // Get a genre by ID
    public Optional<Genre> getGenreById(Long id) {
        return genreRepository.findById(id);
    }

    // Delete a genre by ID
    public void deleteGenre(Long id) {
        genreRepository.deleteById(id);
    }
}
