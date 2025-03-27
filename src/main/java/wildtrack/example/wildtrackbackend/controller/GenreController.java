package wildtrack.example.wildtrackbackend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import wildtrack.example.wildtrackbackend.entity.Genre;
import wildtrack.example.wildtrackbackend.service.GenreService;

@RestController
@RequestMapping("/api/genres")
public class GenreController {

    @Autowired
    private GenreService genreService;

    // Add a new genre
    @PostMapping
    public ResponseEntity<?> addGenre(@RequestBody Genre genre) {
        try {
            Genre savedGenre = genreService.addGenre(genre);
            return ResponseEntity.ok(savedGenre);
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to add genre: " + e.getMessage()));
        }
    }

    // Get all genres
    @GetMapping
    public ResponseEntity<?> getAllGenres() {
        try {
            List<Genre> genres = genreService.getAllGenres();
            return ResponseEntity.ok(genres);
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to retrieve genres: " + e.getMessage()));
        }
    }

    // Get a genre by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getGenreById(@PathVariable Long id) {
        try {
            Optional<Genre> genre = genreService.getGenreById(id);
            if (genre.isPresent()) {
                return ResponseEntity.ok(genre.get());
            } else {
                return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Genre not found with id: " + id));
            }
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to retrieve genre: " + e.getMessage()));
        }
    }

    // Update a genre by ID
    @PutMapping("/{id}")
    public ResponseEntity<?> updateGenre(@PathVariable Long id, @RequestBody Genre updatedGenre) {
        try {
            Genre result = genreService.updateGenre(id, updatedGenre);
            
            // Using HashMap instead of Map.of() to avoid limitations
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Genre updated successfully");
            response.put("genre", result);
            response.put("booksUpdated", true);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to update genre: " + e.getMessage()));
        }
    }

    // Archive/Unarchive a genre by ID
    @PutMapping("/{id}/archive")
    public ResponseEntity<?> toggleArchiveGenre(@PathVariable Long id, @RequestBody Map<String, Boolean> requestBody) {
        try {
            boolean archive = requestBody.getOrDefault("archived", true);
            Genre result = genreService.setArchiveStatus(id, archive);
            
            String action = archive ? "archived" : "unarchived";
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Genre " + action + " successfully");
            response.put("genre", result);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to update archive status: " + e.getMessage()));
        }
    }

    // Delete a genre by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGenre(@PathVariable Long id) {
        try {
            Optional<Genre> genre = genreService.getGenreById(id);
            if (genre.isPresent()) {
                genreService.deleteGenre(id);
                return ResponseEntity.ok(Map.of("message", "Genre deleted successfully"));
            } else {
                return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Genre not found with id: " + id));
            }
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to delete genre: " + e.getMessage()));
        }
    }
}
