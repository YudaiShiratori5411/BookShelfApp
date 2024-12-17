package com.example.bookshelf.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bookshelf.entity.Book;
import com.example.bookshelf.entity.Book.ReadingStatus;
import com.example.bookshelf.service.BookService;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "http://localhost:8080")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    // 本の新規登録
    @PostMapping
    public ResponseEntity<Book> createBook(@RequestBody Book book) {
        Book savedBook = bookService.saveBook(book);
        return ResponseEntity.ok(savedBook);
    }

    // 本の情報更新
    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @RequestBody Book book) {
        try {
            Book updatedBook = bookService.updateBook(id, book);
            return ResponseEntity.ok(updatedBook);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 読書進捗の更新
    @PatchMapping("/{id}/progress")
    public ResponseEntity<Book> updateReadingProgress(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> progress) {
        try {
            Book updatedBook = bookService.updateReadingProgress(id, progress.get("currentPage"));
            return ResponseEntity.ok(updatedBook);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // メモの更新
    @PatchMapping("/{id}/memo")
    public ResponseEntity<Book> updateMemo(
            @PathVariable Long id,
            @RequestBody Map<String, String> memo) {
        try {
            Book updatedBook = bookService.updateMemo(id, memo.get("memo"));
            return ResponseEntity.ok(updatedBook);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 本の削除
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        try {
            bookService.deleteBook(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 全ての本を取得
    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        List<Book> books = bookService.getAllBooks();
        return ResponseEntity.ok(books);
    }

    // IDで本を検索
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        return bookService.getBookById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // カテゴリーで本を検索
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Book>> getBooksByCategory(@PathVariable String category) {
        List<Book> books = bookService.getBooksByCategory(category);
        return ResponseEntity.ok(books);
    }

    // 読書状態で本を検索
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Book>> getBooksByStatus(@PathVariable ReadingStatus status) {
        List<Book> books = bookService.getBooksByReadingStatus(status);
        return ResponseEntity.ok(books);
    }

    // 進捗率で本を検索
    @GetMapping("/progress/{percentage}")
    public ResponseEntity<List<Book>> getBooksByProgress(@PathVariable double percentage) {
        List<Book> books = bookService.getBooksByProgressGreaterThan(percentage);
        return ResponseEntity.ok(books);
    }

    // 最近更新された本を取得
    @GetMapping("/recent")
    public ResponseEntity<List<Book>> getRecentlyUpdatedBooks() {
        List<Book> books = bookService.getRecentlyUpdatedBooks();
        return ResponseEntity.ok(books);
    }
}

