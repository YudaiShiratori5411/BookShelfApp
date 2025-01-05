package com.example.bookshelf.controller;

import java.util.List;
import java.util.stream.Collectors;

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
        List<Shelf> shelves = shelfService.getAllShelvesWithBooks();
        System.out.println("Total shelves: " + shelves.size());
        model.addAttribute("shelves", shelves);
        return "books/list";
    }

    @GetMapping("/new")
    public String newBookForm(Model model) {
        // 本の新規インスタンスを作成
        model.addAttribute("book", new Book());
        
        // 全ての本棚（カテゴリー）を取得して追加
        List<Shelf> shelves = shelfService.getAllShelves();
        model.addAttribute("categories", shelves.stream()
            .map(Shelf::getName)
            .collect(Collectors.toList()));
            
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
           
           // 本の情報をモデルに追加
           model.addAttribute("book", book);
           
           // 読書状態の選択肢を追加
           model.addAttribute("readingStatuses", Book.ReadingStatus.values());
           
           // カテゴリーリストを動的に取得
           List<Shelf> shelves = shelfService.getAllShelves();
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






