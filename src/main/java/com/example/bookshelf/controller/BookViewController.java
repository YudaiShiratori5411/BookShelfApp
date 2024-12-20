package com.example.bookshelf.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.bookshelf.entity.Book;
import com.example.bookshelf.entity.Shelf;
import com.example.bookshelf.service.BookService;
import com.example.bookshelf.service.ShelfService;

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
    public String listBooks(Model model) {
        List<Shelf> shelves = shelfService.getAllShelves();
        model.addAttribute("shelves", shelves);
        return "books/list";
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
       try {
           Book book = bookService.getBookById(id)
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
   public String editBookForm(@PathVariable Long id, Model model) {
       try {
           Book book = bookService.getBookById(id)
                   .orElseThrow(() -> new RuntimeException("本が見つかりません: " + id));
           model.addAttribute("book", book);
           model.addAttribute("readingStatuses", Book.ReadingStatus.values());
           model.addAttribute("categories", Arrays.asList("技術書", "小説", "ビジネス", "その他"));
           return "books/edit";
       } catch (Exception e) {
           model.addAttribute("error", "編集画面の表示に失敗しました");
           return "redirect:/books";
       }
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
   
   @GetMapping("/search")
   public String searchBooks(
       @RequestParam(required = false, defaultValue = "") String title,
       @RequestParam(required = false, defaultValue = "") String author,
       @RequestParam(required = false, defaultValue = "") String category,
       Model model
   ) {
       List<Book> searchResults = bookService.searchBooks(title, author, category);
       model.addAttribute("books", searchResults);
       return "books/list";
   }
   
}
















//package com.example.bookshelf.controller;
//
//import java.util.Arrays;
//import java.util.List;
//
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.ModelAttribute;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import com.example.bookshelf.entity.Book;
//import com.example.bookshelf.service.BookService;
//
//@Controller
//@RequestMapping("/books")
//public class BookViewController {
//
//    private final BookService bookService;
//
//    public BookViewController(BookService bookService) {
//        this.bookService = bookService;
//    }
//
//    @GetMapping
//    public String listBooks(Model model) {
//        List<Book> books = bookService.getAllBooks();
//        model.addAttribute("books", books);
//        return "books/list";
//    }
//
//    @GetMapping("/new")
//    public String newBookForm(Model model) {
//        model.addAttribute("book", new Book());
//        return "books/form";
//    }
//    
//    @PostMapping
//    public String createBook(@ModelAttribute Book book, RedirectAttributes redirectAttributes) {
//        try {
//            bookService.saveBook(book);
//            redirectAttributes.addFlashAttribute("message", "本を登録しました");
//            redirectAttributes.addFlashAttribute("messageType", "success");
//            return "redirect:/books";
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("message", "登録に失敗しました");
//            redirectAttributes.addFlashAttribute("messageType", "error");
//            return "redirect:/books/new";
//        }
//    }
//    
//    @GetMapping("/{id}")
//    public String showBookDetail(@PathVariable Long id, Model model) {
//        try {
//            Book book = bookService.getBookById(id)
//                    .orElseThrow(() -> new RuntimeException("本が見つかりません: " + id));
//            model.addAttribute("book", book);
//            return "books/detail";
//        } catch (Exception e) {
//            model.addAttribute("message", "本の取得に失敗しました");
//            model.addAttribute("messageType", "error");
//            return "redirect:/books";
//        }
//    }
//
//    @GetMapping("/{id}/edit")
//    public String editBookForm(@PathVariable Long id, Model model) {
//        try {
//            Book book = bookService.getBookById(id)
//                    .orElseThrow(() -> new RuntimeException("本が見つかりません: " + id));
//            System.out.println("Book for edit: " + book); // デバッグログ
//            model.addAttribute("book", book);
//            model.addAttribute("readingStatuses", Book.ReadingStatus.values());
//            model.addAttribute("categories", Arrays.asList("技術書", "小説", "ビジネス", "その他"));
//            return "books/edit";
//        } catch (Exception e) {
//            System.err.println("Error loading book for edit: " + e.getMessage()); // エラーログ
//            e.printStackTrace();
//            model.addAttribute("error", "編集画面の表示に失敗しました");
//            return "redirect:/books";
//        }
//    }
//
//    @PostMapping("/{id}/update")
//    public String updateBook(@PathVariable Long id, @ModelAttribute Book book, RedirectAttributes redirectAttributes) {
//        try {
//            Book existingBook = bookService.getBookById(id)
//                    .orElseThrow(() -> new RuntimeException("本が見つかりません: " + id));
//            
//            // 既存の値を保持
//            book.setId(id);
//            book.setCreatedAt(existingBook.getCreatedAt());
//            
//            // 更新処理
//            bookService.updateBook(id, book);
//            
//            redirectAttributes.addFlashAttribute("message", "本を更新しました");
//            redirectAttributes.addFlashAttribute("messageType", "success");
//            return "redirect:/books/" + id;
//        } catch (Exception e) {
//            e.printStackTrace(); // エラーログの出力
//            redirectAttributes.addFlashAttribute("message", "更新に失敗しました");
//            redirectAttributes.addFlashAttribute("messageType", "error");
//            return "redirect:/books/" + id + "/edit";
//        }
//    }
//    
//    @PostMapping("/{id}/delete")
//    public String deleteBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
//        try {
//            bookService.deleteBook(id);
//            redirectAttributes.addFlashAttribute("message", "本を削除しました");
//            redirectAttributes.addFlashAttribute("messageType", "success");
//            return "redirect:/books";
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("message", "削除に失敗しました");
//            redirectAttributes.addFlashAttribute("messageType", "error");
//            return "redirect:/books/" + id;
//        }
//    }
//    
//    @GetMapping("/search")
//    public String searchBooks(
//        @RequestParam(required = false, defaultValue = "") String title,
//        @RequestParam(required = false, defaultValue = "") String author,
//        @RequestParam(required = false, defaultValue = "") String category,
//        Model model
//    ) {
//        List<Book> searchResults = bookService.searchBooks(title, author, category);
//        model.addAttribute("books", searchResults);
//        return "books/list";
//    }
//}

