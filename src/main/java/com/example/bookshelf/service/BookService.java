package com.example.bookshelf.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bookshelf.entity.Book;
import com.example.bookshelf.entity.Book.ReadingStatus;
import com.example.bookshelf.repository.BookRepository;

@Service
@Transactional
public class BookService {
    
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    // 本の登録
    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }

    // 本の情報更新
    public Book updateBook(Long id, Book bookDetails) {
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("本が見つかりません: " + id));
        
        book.setTitle(bookDetails.getTitle());
        book.setAuthor(bookDetails.getAuthor());
        book.setCategory(bookDetails.getCategory());
        book.setTotalPages(bookDetails.getTotalPages());
        book.setCurrentPage(bookDetails.getCurrentPage());
        book.setMemo(bookDetails.getMemo());
        book.setReadingStatus(bookDetails.getReadingStatus());
        
        return bookRepository.save(book);
    }

    // 読書進捗の更新
    public Book updateReadingProgress(Long id, Integer currentPage) {
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("本が見つかりません: " + id));
        
        book.setCurrentPage(currentPage);
        
        // 読書状態の自動更新
        if (currentPage >= book.getTotalPages()) {
            book.setReadingStatus(ReadingStatus.COMPLETED);
        } else if (currentPage > 0) {
            book.setReadingStatus(ReadingStatus.READING);
        }
        
        return bookRepository.save(book);
    }

    // メモの更新
    public Book updateMemo(Long id, String memo) {
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("本が見つかりません: " + id));
        
        book.setMemo(memo);
        return bookRepository.save(book);
    }

    // 本の削除
    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }

    // 本の取得（ID指定）
    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    // 全ての本を取得
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    // カテゴリーで本を検索
    public List<Book> getBooksByCategory(String category) {
        return bookRepository.findByCategory(category);
    }

    // 読書状態で本を検索
    public List<Book> getBooksByReadingStatus(ReadingStatus status) {
        return bookRepository.findByReadingStatus(status);
    }

    // 進捗率で本を検索
    public List<Book> getBooksByProgressGreaterThan(double percentage) {
        return bookRepository.findByReadingProgressGreaterThan(percentage);
    }

    // 最近更新された本を取得
    public List<Book> getRecentlyUpdatedBooks() {
        return bookRepository.findTop10ByOrderByUpdatedAtDesc();
    }
    
    // 検索メソッド
    public List<Book> searchBooks(String title, String author, String category) {
        return bookRepository.searchBooks(
            title.isEmpty() ? null : title,
            author.isEmpty() ? null : author,
            category.isEmpty() ? null : category
        );
    }
}


