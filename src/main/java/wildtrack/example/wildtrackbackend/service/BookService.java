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

    // Check if a book exists by accession number
    public boolean existsByAccessionNumber(String accessionNumber) {
        return bookRepository.existsByAccessionNumber(accessionNumber);
    }

    // Check if a book exists by ISBN
    public boolean existsByIsbn(String isbn) {
        return bookRepository.existsByIsbn(isbn);
    }

    // Fetch a book by ID
    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }
}
