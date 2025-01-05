package com.example.bookshelf.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bookshelf.dto.ReorderBooksRequest;
import com.example.bookshelf.entity.Book;
import com.example.bookshelf.entity.Book.ReadingStatus;
import com.example.bookshelf.entity.Divider;
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
        // カテゴリーに基づいて適切な本棚を検索
        Shelf shelf = shelfRepository.findByName(book.getCategory())
                .orElseThrow(() -> new RuntimeException("該当する本棚が見つかりません: " + book.getCategory()));
        
        // 本棚を設定
        book.setShelf(shelf);
        
        // その本棚の最大position値を取得し、新しい本のpositionを設定
        Integer maxPosition = bookRepository.findMaxPositionByShelfId(shelf.getId());
        book.setPosition(maxPosition != null ? maxPosition + 1 : 0);
        
        return bookRepository.save(book);
    }

    // 本の情報更新
    @Transactional
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
        book.setCoverImage(bookDetails.getCoverImage());
        
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
        try {
            return bookRepository.findById(id);
        } catch (Exception e) {
            System.err.println("Error in getBookById: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
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
    
    @Transactional(readOnly = true)
    public List<Book> searchBooks(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        String searchQuery = query.toLowerCase();
        try {
            return bookRepository.findAll().stream()
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
    
    // ReadingStatusの更新メソッドを追加
    public Book updateReadingStatus(Long id, ReadingStatus status) {
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("本が見つかりません: " + id));
        
        book.setReadingStatus(status);
        return bookRepository.save(book);
    }
    
    // 順序保存のメソッド
    @Transactional
    public void reorderBooks(String shelfId, List<ReorderBooksRequest.BookPosition> positions) {
        Long shelfIdLong = Long.parseLong(shelfId);
        
        // 本棚の全アイテム（本と仕切り）を取得して整理
        List<Book> existingBooks = bookRepository.findAllByShelf_IdOrderByPosition(shelfIdLong);
        List<Divider> existingDividers = dividerRepository.findAllByShelf_IdOrderByPositionAsc(shelfIdLong);
        
        // 一時的な位置を使用（競合を避けるため）
        int offset = 10000;
        positions.forEach(pos -> {
            Long bookId = Long.parseLong(pos.getId());
            bookRepository.updatePosition(bookId, offset + pos.getPosition());
        });
        bookRepository.flush();

        // 仕切りを含めた全体の位置を再計算
        Map<Integer, Long> desiredPositions = new HashMap<>();
        positions.forEach(pos -> {
            desiredPositions.put(pos.getPosition(), Long.parseLong(pos.getId()));
        });

        // 位置を最終的な値に更新
        positions.forEach(pos -> {
            Long bookId = Long.parseLong(pos.getId());
            Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + bookId));
            
            bookRepository.updatePosition(bookId, pos.getPosition());
        });
        
        bookRepository.flush();
    }
}


