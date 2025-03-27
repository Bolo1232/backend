package wildtrack.example.wildtrackbackend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import wildtrack.example.wildtrackbackend.entity.Book;
import wildtrack.example.wildtrackbackend.repository.BookRepository;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    // Save a new book
    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }

    // Retrieve all books
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    // Retrieve all books by genre
    public List<Book> getBooksByGenre(String genre) {
        return bookRepository.findByGenre(genre);
    }

    // Check if a book exists by accession number
    public boolean existsByAccessionNumber(String accessionNumber) {
        return bookRepository.existsByAccessionNumber(accessionNumber);
    }

    // Retrieve a book by its ID
    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    // Update an existing book
    public Optional<Book> updateBook(Long id, Book updatedBook) {
        return bookRepository.findById(id).map(existingBook -> {
            existingBook.setAuthor(updatedBook.getAuthor());
            existingBook.setTitle(updatedBook.getTitle());
            existingBook.setAccessionNumber(updatedBook.getAccessionNumber());
            existingBook.setGenre(updatedBook.getGenre());

            // Only update dateRegistered if it's provided
            if (updatedBook.getDateRegistered() != null) {
                existingBook.setDateRegistered(updatedBook.getDateRegistered());
            }

            return bookRepository.save(existingBook);
        });
    }

    // Delete a book by its ID
    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }

    // Delete all books
    public void deleteAllBooks() {
        bookRepository.deleteAll();
    }
}