package com.example.bookshelf.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.bookshelf.controller.form.BookForm;
import com.example.bookshelf.entity.Book;
import com.example.bookshelf.entity.Shelf;
import com.example.bookshelf.service.BookService;
import com.example.bookshelf.service.ShelfService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/books")
public class BookViewController {
    private final BookService bookService;
    private final ShelfService shelfService;

    public BookViewController(BookService bookService, ShelfService shelfService) {
        this.bookService = bookService;
        this.shelfService = shelfService;
    }

    @GetMapping
    public String listBooks(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        List<Shelf> shelves = shelfService.getAllShelvesWithBooks(userId);
        model.addAttribute("shelves", shelves);
        return "books/list";
    }

    @GetMapping("/new")
    public String newBookForm(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        // BookFormのインスタンスを作成
        model.addAttribute("book", new BookForm());
        
        // カテゴリーリストを取得
        List<Shelf> shelves = shelfService.getAllShelves(userId);
        model.addAttribute("categories", shelves.stream()
            .map(Shelf::getName)
            .collect(Collectors.toList()));
            
        return "books/form";
    }
   
    @PostMapping
    public String createBook(@ModelAttribute("book") @Valid BookForm form, 
                           BindingResult result, 
                           RedirectAttributes redirectAttributes,
                           Model model,
                           HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            List<Shelf> shelves = shelfService.getAllShelves(userId);
            model.addAttribute("categories", shelves.stream()
                .map(Shelf::getName)
                .collect(Collectors.toList()));
            return "books/form";
        }

        try {
            Book book = new Book();
            book.setTitle(form.getTitle());
            book.setAuthor(form.getAuthor());
            book.setCategory(form.getCategory());
            book.setReadingStatus(Book.ReadingStatus.valueOf(form.getReadingStatus()));
            book.setCurrentPage(form.getCurrentPage());
            book.setUserId(userId);  // ユーザーID設定
            
            if ("不明".equals(form.getTotalPages())) {
                book.setTotalPages(null);
            } else {
                book.setTotalPages(Integer.parseInt(form.getTotalPages()));
            }
            
            book.setCoverImage(form.getCoverImage());
            book.setMemo(form.getMemo());

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
    public String showBookDetail(@PathVariable Long id, Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            Book book = bookService.getBookById(id, userId)
                    .orElseThrow(() -> new RuntimeException("本が見つかりません: " + id));
            model.addAttribute("book", book);
            return "books/detail";
        } catch (Exception e) {
            model.addAttribute("message", "本の取得に失敗しました");
            model.addAttribute("messageType", "error");
            return "redirect:/books";
        }
    }
   
    @GetMapping("/{id}/edit")
    public String editBookForm(@PathVariable Long id, Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            Book book = bookService.getBookById(id, userId)
                    .orElseThrow(() -> new RuntimeException("本が見つかりません: " + id));
            
            BookForm form = new BookForm();
            form.setId(id);
            form.setTitle(book.getTitle());
            form.setAuthor(book.getAuthor());
            form.setCategory(book.getCategory());
            form.setReadingStatus(book.getReadingStatus().name());
            form.setCurrentPage(book.getCurrentPage());
            form.setTotalPages(book.getTotalPages() != null ? 
                              book.getTotalPages().toString() : "不明");
            form.setCoverImage(book.getCoverImage());
            form.setMemo(book.getMemo());
            
            model.addAttribute("book", form);
            model.addAttribute("readingStatuses", Book.ReadingStatus.values());
            
            List<Shelf> shelves = shelfService.getAllShelves(userId);
            model.addAttribute("categories", shelves.stream()
                .map(Shelf::getName)
                .collect(Collectors.toList()));
            
            return "books/edit";
        } catch (Exception e) {
            model.addAttribute("error", "編集画面の表示に失敗しました");
            return "redirect:/books";
        }
    }

    @PostMapping("/{id}/update")
    public String updateBook(@PathVariable Long id, 
                           @ModelAttribute("book") @Valid BookForm form,
                           BindingResult result,
                           RedirectAttributes redirectAttributes,
                           Model model,
                           HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            model.addAttribute("readingStatuses", Book.ReadingStatus.values());
            List<Shelf> shelves = shelfService.getAllShelves(userId);
            model.addAttribute("categories", shelves.stream()
                .map(Shelf::getName)
                .collect(Collectors.toList()));
            return "books/edit";
        }

        try {
            Book book = bookService.getBookById(id, userId)
                .orElseThrow(() -> new RuntimeException("本が見つかりません: " + id));
            
            book.setTitle(form.getTitle());
            book.setAuthor(form.getAuthor());
            book.setCategory(form.getCategory());
            book.setReadingStatus(Book.ReadingStatus.valueOf(form.getReadingStatus()));
            book.setCurrentPage(form.getCurrentPage());
            
            if ("不明".equals(form.getTotalPages())) {
                book.setTotalPages(null);
            } else {
                try {
                    book.setTotalPages(Integer.parseInt(form.getTotalPages()));
                } catch (NumberFormatException e) {
                    book.setTotalPages(null);
                }
            }
            
            if (form.getCoverImage() != null && !form.getCoverImage().isEmpty()) {
                book.setCoverImage(form.getCoverImage());
            }
            
            book.setMemo(form.getMemo());

            bookService.updateBook(id, book, userId);
            redirectAttributes.addFlashAttribute("message", "本を更新しました");
            redirectAttributes.addFlashAttribute("messageType", "success");
            return "redirect:/books/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "更新に失敗しました: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/books/" + id + "/edit";
        }
    }
   
    @PostMapping("/{id}/delete")
    public String deleteBook(@PathVariable Long id, 
                           RedirectAttributes redirectAttributes,
                           HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            bookService.deleteBook(id, userId);
            redirectAttributes.addFlashAttribute("message", "本を削除しました");
            redirectAttributes.addFlashAttribute("messageType", "success");
            return "redirect:/books";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "削除に失敗しました");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/books/" + id;
        }
    }
   
    @GetMapping("/search")
    public String searchBooks(
        @RequestParam(required = false, defaultValue = "") String title,
        @RequestParam(required = false, defaultValue = "") String author,
        @RequestParam(required = false, defaultValue = "") String category,
        Model model,
        HttpSession session
    ) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        List<Book> searchResults = bookService.searchBooks(title, author, category, userId);
        model.addAttribute("books", searchResults);
        return "books/list";
    }
}

