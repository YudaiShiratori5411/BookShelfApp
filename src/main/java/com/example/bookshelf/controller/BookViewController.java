package com.example.bookshelf.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.bookshelf.entity.Book;
import com.example.bookshelf.service.BookService;

@Controller
@RequestMapping("/books")
public class BookViewController {

    private final BookService bookService;

    public BookViewController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public String listBooks(Model model) {
        List<Book> books = bookService.getAllBooks();
        // 全ての本からユニークなカテゴリーのリストを取得
        List<String> categories = books.stream()
            .map(Book::getCategory)
            .distinct()
            .collect(Collectors.toList());
        
        model.addAttribute("books", books);
        model.addAttribute("categories", categories);
        return "books/list";
    }

    @GetMapping("/new")
    public String newBookForm(Model model) {
        model.addAttribute("book", new Book());
        return "books/form";
    }
}

