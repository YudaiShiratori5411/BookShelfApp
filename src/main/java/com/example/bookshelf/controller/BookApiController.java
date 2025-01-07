package com.example.bookshelf.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.bookshelf.dto.ReorderBooksRequest;
import com.example.bookshelf.entity.Book;
import com.example.bookshelf.entity.Book.ReadingStatus;
import com.example.bookshelf.service.BookService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:8080")
public class BookApiController {
    
    @Value("${rakuten.application-id}")
    private String applicationId;

    @Value("${rakuten.books.api-url}")
    private String apiUrl;
    
    private final RestTemplate restTemplate;
    private final BookService bookService;

    public BookApiController(RestTemplate restTemplate, BookService bookService) {
        this.restTemplate = restTemplate;
        this.bookService = bookService;
    }

    @PostMapping
    public ResponseEntity<Book> createBook(@RequestBody Book book) {
        Book savedBook = bookService.saveBook(book);
        return ResponseEntity.ok(savedBook);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @RequestBody Book bookDetails) {
        try {
            Book updatedBook = bookService.updateBook(id, bookDetails);
            return ResponseEntity.ok(updatedBook);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        try {
            bookService.deleteBook(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        List<Book> books = bookService.getAllBooks();
        return ResponseEntity.ok(books);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        return bookService.getBookById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Book>> getBooksByCategory(@PathVariable String category) {
        List<Book> books = bookService.getBooksByCategory(category);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Book>> getBooksByStatus(@PathVariable ReadingStatus status) {
        List<Book> books = bookService.getBooksByReadingStatus(status);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/progress/{percentage}")
    public ResponseEntity<List<Book>> getBooksByProgress(@PathVariable double percentage) {
        List<Book> books = bookService.getBooksByProgressGreaterThan(percentage);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Book>> getRecentlyUpdatedBooks() {
        List<Book> books = bookService.getRecentlyUpdatedBooks();
        return ResponseEntity.ok(books);
    }
    
    @PostMapping("/reorder")
    public ResponseEntity<?> reorderBooks(@RequestBody ReorderBooksRequest request) {
        try {
            bookService.reorderBooks(request.getShelfId(), request.getBookPositions());
            return ResponseEntity.ok(Map.of("message", "順序を保存しました"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/search")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Book>> searchBooks(@RequestParam(required = false) String query) {
        try {
            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Collections.emptyList());
            }

            List<Book> results = bookService.searchBooks(query.trim());
            
            List<Map<String, Object>> simplifiedResults = results.stream()
                .map(book -> {
                    Map<String, Object> simplifiedBook = new HashMap<>();
                    simplifiedBook.put("id", book.getId());
                    simplifiedBook.put("title", book.getTitle());
                    simplifiedBook.put("author", book.getAuthor());
                    simplifiedBook.put("totalPages", book.getTotalPages());
                    simplifiedBook.put("currentPage", book.getCurrentPage());
                    simplifiedBook.put("readingStatus", book.getReadingStatus());
                    return simplifiedBook;
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(results);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Collections.emptyList());
        }
    }
    
    @GetMapping("/books/rakuten")
    public ResponseEntity<?> getRakutenBookInfo(@RequestParam String isbn) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .queryParam("format", "json")
                .queryParam("isbn", isbn)
                .queryParam("applicationId", applicationId)
                .build()
                .toUriString();

            System.out.println("Requesting Rakuten API URL: " + url);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            System.out.println("Rakuten API Response: " + response.getBody());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            if (root.has("Items") && root.get("Items").size() > 0) {
                JsonNode item = root.get("Items").get(0).get("Item");
                
                Map<String, Object> bookInfo = new HashMap<>();
                bookInfo.put("success", true);
                bookInfo.put("title", item.get("title").asText());
                bookInfo.put("author", item.get("author").asText());
                bookInfo.put("pageCount", extractPageCount(item.get("size").asText()));
                bookInfo.put("coverUrl", item.get("largeImageUrl").asText());

                return ResponseEntity.ok(bookInfo);
            }

            return ResponseEntity.ok(Map.of("success", false));

        } catch (Exception e) {
            System.err.println("Rakuten API Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    private String extractPageCount(String size) {
        if (size == null || size.isEmpty()) {
            return "不明";
        }
        Pattern pattern = Pattern.compile("(\\d+)p");
        Matcher matcher = pattern.matcher(size);
        return matcher.find() ? matcher.group(1) : "不明";
    }

    @GetMapping("/books/google")
    public ResponseEntity<?> getGoogleBookInfo(@RequestParam String isbn) {
        try {
            // ISBNのフォーマットを調整（ハイフンを削除）
            String cleanIsbn = isbn.replaceAll("-", "");
            String url = String.format("https://www.googleapis.com/books/v1/volumes?q=isbn:%s&country=JP", cleanIsbn);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.USER_AGENT, "Mozilla/5.0");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
            );
            
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            System.err.println("Google Books API Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
}

