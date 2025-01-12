package com.example.bookshelf.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bookshelf.dto.ReorderBooksRequest;
import com.example.bookshelf.entity.Book;
import com.example.bookshelf.entity.Book.ReadingStatus;
import com.example.bookshelf.entity.Shelf;
import com.example.bookshelf.repository.BookRepository;
import com.example.bookshelf.repository.DividerRepository;
import com.example.bookshelf.repository.ShelfRepository;

@Service
@Transactional
public class BookService {
    private final BookRepository bookRepository;
    private final ShelfRepository shelfRepository;
    private final DividerRepository dividerRepository;

    public BookService(BookRepository bookRepository, 
                      ShelfRepository shelfRepository,
                      DividerRepository dividerRepository) {
        this.bookRepository = bookRepository;
        this.shelfRepository = shelfRepository;
        this.dividerRepository = dividerRepository;
    }
    
    @Transactional
    public Book saveBook(Book book) {
        // カテゴリーと所有者に基づいて適切な本棚を検索
        Shelf shelf = shelfRepository.findByNameAndUserId(book.getCategory(), book.getUserId())
                .orElseThrow(() -> new RuntimeException("該当する本棚が見つかりません: " + book.getCategory()));
        
        // 本棚を設定
        book.setShelf(shelf);
        
        // その本棚の最大position値を取得し、新しい本のpositionを設定
        Integer maxPosition = bookRepository.findMaxPositionByShelfIdAndUserId(shelf.getId(), book.getUserId());
        book.setPosition(maxPosition != null ? maxPosition + 1 : 0);
        
        return bookRepository.save(book);
    }

    // 本の情報更新
    @Transactional
    public Book updateBook(Long id, Book bookDetails, Long userId) {
        Book book = bookRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("本が見つかりません: " + id));
        
        book.setTitle(bookDetails.getTitle());
        book.setAuthor(bookDetails.getAuthor());
        book.setCategory(bookDetails.getCategory());
        book.setTotalPages(bookDetails.getTotalPages());
        book.setCurrentPage(bookDetails.getCurrentPage());
        book.setMemo(bookDetails.getMemo());
        book.setReadingStatus(bookDetails.getReadingStatus());
        book.setCoverImage(bookDetails.getCoverImage());
        
        return bookRepository.save(book);
    }

    // 読書進捗の更新
    public Book updateReadingProgress(Long id, Integer currentPage, Long userId) {
        Book book = bookRepository.findByIdAndUserId(id, userId)
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
    public Book updateMemo(Long id, String memo, Long userId) {
        Book book = bookRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new RuntimeException("本が見つかりません: " + id));
        
        book.setMemo(memo);
        return bookRepository.save(book);
    }

    // 本の削除
    public void deleteBook(Long id, Long userId) {
        if (!bookRepository.existsByIdAndUserId(id, userId)) {
            throw new RuntimeException("本が見つかりません: " + id);
        }
        bookRepository.deleteByIdAndUserId(id, userId);
    }

    // 本の取得（ID指定）
    public Optional<Book> getBookById(Long id, Long userId) {
        try {
            return bookRepository.findByIdAndUserId(id, userId);
        } catch (Exception e) {
            System.err.println("Error in getBookById: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    // ユーザーの全ての本を取得
    public List<Book> getAllBooks(Long userId) {
        return bookRepository.findByUserId(userId);
    }

    // カテゴリーで本を検索
    public List<Book> getBooksByCategory(String category, Long userId) {
        return bookRepository.findByCategoryAndUserId(category, userId);
    }

    // 読書状態で本を検索
    public List<Book> getBooksByReadingStatus(ReadingStatus status, Long userId) {
        return bookRepository.findByReadingStatusAndUserId(status, userId);
    }

    // 進捗率で本を検索
    public List<Book> getBooksByProgressGreaterThan(double percentage, Long userId) {
        return bookRepository.findByReadingProgressGreaterThanAndUserId(percentage, userId);
    }

    // 最近更新された本を取得
    public List<Book> getRecentlyUpdatedBooks(Long userId) {
        return bookRepository.findTop10ByUserIdOrderByUpdatedAtDesc(userId);
    }
    
    // 検索メソッド
    public List<Book> searchBooks(String title, String author, String category, Long userId) {
        return bookRepository.searchBooksByUserId(
            title.isEmpty() ? null : title,
            author.isEmpty() ? null : author,
            category.isEmpty() ? null : category,
            userId
        );
    }
    
    @Transactional(readOnly = true)
    public List<Book> searchBooks(String query, Long userId) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        String searchQuery = query.toLowerCase();
        try {
            return bookRepository.findByUserId(userId).stream()
                .filter(book -> 
                    (book.getTitle() != null && book.getTitle().toLowerCase().contains(searchQuery)) ||
                    (book.getAuthor() != null && book.getAuthor().toLowerCase().contains(searchQuery)))
                .collect(Collectors.collectingAndThen(
                    Collectors.toList(),
                    Collections::unmodifiableList
                ));
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    
    // ReadingStatusの更新メソッド
    public Book updateReadingStatus(Long id, ReadingStatus status, Long userId) {
        Book book = bookRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new RuntimeException("本が見つかりません: " + id));
        
        book.setReadingStatus(status);
        return bookRepository.save(book);
    }
    
    // 順序保存のメソッド
    @Transactional
    public void reorderBooks(String shelfId, List<ReorderBooksRequest.BookPosition> positions, Long userId) {
        Long shelfIdLong = Long.parseLong(shelfId);
        Shelf shelf = shelfRepository.findByIdAndUserId(shelfIdLong, userId)
            .orElseThrow(() -> new IllegalArgumentException("Shelf not found: " + shelfId));

        // 一時的な位置を使用（競合を避けるため）
        int offset = 10000;
        positions.forEach(pos -> {
            Long bookId = Long.parseLong(pos.getId());
            Book book = bookRepository.findByIdAndUserId(bookId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + bookId));
            
            // 本棚が変更された場合は更新
            if (!book.getShelf().getId().equals(shelfIdLong)) {
                book.setShelf(shelf);
            }
            book.setPosition(offset + pos.getPosition());
            bookRepository.save(book);
        });
        bookRepository.flush();

        // 最終的な位置に更新
        positions.forEach(pos -> {
            Long bookId = Long.parseLong(pos.getId());
            bookRepository.updatePositionForUser(bookId, pos.getPosition(), userId);
        });
        
        bookRepository.flush();
    }
}


