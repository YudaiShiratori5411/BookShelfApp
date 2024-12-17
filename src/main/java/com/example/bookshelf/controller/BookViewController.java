package com.example.bookshelf.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        model.addAttribute("books", books);
        model.addAttribute("template", "books/list");
        return "layouts/layout";
    }

    @GetMapping("/new")
    public String newBookForm(Model model) {
        model.addAttribute("book", new Book());
        return "books/form";
    }
    
    @PostMapping
    public String createBook(@ModelAttribute Book book, RedirectAttributes redirectAttributes) {
        try {
            bookService.saveBook(book);
            redirectAttributes.addFlashAttribute("message", "本を登録しました");
            redirectAttributes.addFlashAttribute("messageType", "success");
            return "redirect:/books";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "登録に失敗しました");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/books/new";
        }
    }
    
    @GetMapping("/{id}")
    public String showBookDetail(@PathVariable Long id, Model model) {
        Book book = bookService.getBookById(id)
                .orElseThrow(() -> new RuntimeException("本が見つかりません: " + id));
        model.addAttribute("book", book);
        model.addAttribute("template", "books/detail");
        return "layouts/layout";
    }
    
    @GetMapping("/{id}/edit")
    public String editBookForm(@PathVariable Long id, Model model) {
        Book book = bookService.getBookById(id)
                .orElseThrow(() -> new RuntimeException("本が見つかりません: " + id));
        model.addAttribute("book", book);
        model.addAttribute("template", "books/edit");
        return "layouts/layout";
    }

    @PostMapping("/{id}/update")
    public String updateBook(@PathVariable Long id, @ModelAttribute Book book, RedirectAttributes redirectAttributes) {
        try {
            bookService.updateBook(id, book);
            redirectAttributes.addFlashAttribute("message", "本を更新しました");
            redirectAttributes.addFlashAttribute("messageType", "success");
            return "redirect:/books/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "更新に失敗しました");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/books/" + id + "/edit";
        }
    }
    
    @PostMapping("/{id}/delete")
    public String deleteBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookService.deleteBook(id);
            redirectAttributes.addFlashAttribute("message", "本を削除しました");
            redirectAttributes.addFlashAttribute("messageType", "success");
            return "redirect:/books";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "削除に失敗しました");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/books/" + id;
        }
    }
}

