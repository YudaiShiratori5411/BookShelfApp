package com.example.bookshelf.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import com.example.bookshelf.entity.Book;
import com.example.bookshelf.entity.ReadingSession;
import com.example.bookshelf.service.BookService;
import com.example.bookshelf.service.ReadingSessionService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/books/{bookId}/progress")
public class ReadingProgressController {

   @Autowired
   private ReadingSessionService readingSessionService;

   @Autowired
   private BookService bookService;

   // 進捗画面の表示
   @GetMapping
   public String showProgress(@PathVariable Long bookId,
                            @RequestParam(defaultValue = "0") int page,
                            Model model,
                            HttpSession session) {
       Long userId = (Long) session.getAttribute("userId");
       if (userId == null) {
           return "redirect:/login";
       }

       // 1ページあたりの表示件数
       final int PAGE_SIZE = 15;

       Book book = bookService.getBookById(bookId, userId)
               .orElseThrow(() -> new IllegalArgumentException("Book not found with id: " + bookId));

       // ページネーション付きでセッションを取得
       Page<ReadingSession> sessionPage = readingSessionService.getSessionsByBookPaged(book, page, PAGE_SIZE);

       // セッションデータのタイムゾーン調整
       List<ReadingSession> adjustedSessions = sessionPage.getContent().stream()
           .map(readingSession -> {
               ReadingSession adjustedSession = new ReadingSession();
               adjustedSession.setId(readingSession.getId());
               adjustedSession.setBook(readingSession.getBook());
               
               // 開始時間の調整（UTCからローカル時間に変換）
               LocalDateTime localStartTime = readingSession.getStartTime()
                   .atZone(ZoneId.systemDefault())
                   .toLocalDateTime();
               adjustedSession.setStartTime(localStartTime);
               
               // 終了時間の調整（UTCからローカル時間に変換）
               LocalDateTime localEndTime = readingSession.getEndTime()
                   .atZone(ZoneId.systemDefault())
                   .toLocalDateTime();
               adjustedSession.setEndTime(localEndTime);
               
               adjustedSession.setPagesRead(readingSession.getPagesRead());
               return adjustedSession;
           })
           .collect(Collectors.toList());

       model.addAttribute("book", book);
       model.addAttribute("sessions", adjustedSessions);
       model.addAttribute("currentPage", page);
       model.addAttribute("totalPages", sessionPage.getTotalPages());

       // 平均ページ数を計算
       double averagePages = readingSessionService.calculateAveragePagesPerDay(book);
       model.addAttribute("averagePages", String.format("%.1f", averagePages));

       // 予測読了日を計算（タイムゾーン調整済みの日時を使用）
       LocalDateTime predictedDate = readingSessionService.calculatePredictedFinishDate(book)
               .atZone(ZoneOffset.UTC)
               .withZoneSameInstant(ZoneId.systemDefault())
               .toLocalDateTime();
       model.addAttribute("predictedDate", predictedDate);

       return "books/progress";
   }

   // 新しいセッションの記録
   @PostMapping
   public String recordSession(@PathVariable Long bookId,
                             @RequestParam LocalDateTime startTime,
                             @RequestParam LocalDateTime endTime,
                             @RequestParam Integer pagesRead,
                             HttpSession session) {
       Long userId = (Long) session.getAttribute("userId");
       if (userId == null) {
           return "redirect:/login";
       }

       Book book = bookService.getBookById(bookId, userId)
               .orElseThrow(() -> new IllegalArgumentException("Book not found with id: " + bookId));

       ReadingSession readingSession = new ReadingSession();
       readingSession.setBook(book);
       readingSession.setStartTime(startTime);
       readingSession.setEndTime(endTime);
       readingSession.setPagesRead(pagesRead);

       readingSessionService.saveSession(readingSession);

       return "redirect:/books/" + bookId + "/progress";
   }

   // セッションの削除
   @DeleteMapping("/sessions/{sessionId}")
   public String deleteSession(@PathVariable Long bookId, 
                             @PathVariable Long sessionId,
                             HttpSession session) {
       Long userId = (Long) session.getAttribute("userId");
       if (userId == null) {
           return "redirect:/login";
       }

       readingSessionService.deleteSession(sessionId);
       return "redirect:/books/" + bookId + "/progress";
   }

   // セッションの更新
   @PutMapping("/sessions/{sessionId}")
   public String updateSession(@PathVariable Long bookId,
                             @PathVariable Long sessionId,
                             @RequestParam LocalDateTime startTime,
                             @RequestParam LocalDateTime endTime,
                             @RequestParam Integer pagesRead,
                             HttpSession session) {
       Long userId = (Long) session.getAttribute("userId");
       if (userId == null) {
           return "redirect:/login";
       }
       
       ReadingSession readingSession = new ReadingSession();
       readingSession.setId(sessionId);
       readingSession.setStartTime(startTime);
       readingSession.setEndTime(endTime);
       readingSession.setPagesRead(pagesRead);
       
       readingSessionService.updateSession(sessionId, readingSession);
       
       return "redirect:/books/" + bookId + "/progress";
   }
   
   @GetMapping("/sessions/{sessionId}")
   @ResponseBody
   public ResponseEntity<ReadingSession> getSession(@PathVariable Long bookId, 
                                                  @PathVariable Long sessionId,
                                                  HttpSession session) {
       Long userId = (Long) session.getAttribute("userId");
       if (userId == null) {
           return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
       }

       ReadingSession readingSession = readingSessionService.getSessionById(sessionId)
               .orElseThrow(() -> new ResponseStatusException(
                   HttpStatus.NOT_FOUND, 
                   "Session not found with id: " + sessionId
               ));
       return ResponseEntity.ok(readingSession);
   }
}
