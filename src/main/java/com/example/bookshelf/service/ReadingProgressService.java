package com.example.bookshelf.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bookshelf.entity.Book;
import com.example.bookshelf.entity.ReadingGoal;
import com.example.bookshelf.entity.ReadingSession;
import com.example.bookshelf.repository.BookRepository;
import com.example.bookshelf.repository.ReadingGoalRepository;
import com.example.bookshelf.repository.ReadingSessionRepository;

@Service
@Transactional
public class ReadingProgressService {
    private final ReadingSessionRepository sessionRepository;
    private final ReadingGoalRepository goalRepository;
    private final BookRepository bookRepository;
    
    public ReadingProgressService(
            ReadingSessionRepository sessionRepository,
            ReadingGoalRepository goalRepository,
            BookRepository bookRepository) {
        this.sessionRepository = sessionRepository;
        this.goalRepository = goalRepository;
        this.bookRepository = bookRepository;
    }

    // 読書セッションの記録
    public ReadingSession recordSession(Long bookId, LocalDateTime startTime, LocalDateTime endTime, int pagesRead) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("本が見つかりません"));
        
        ReadingSession session = new ReadingSession();
        session.setBook(book);
        session.setStartTime(startTime);
        session.setEndTime(endTime);
        session.setPagesRead(pagesRead);
        
        return sessionRepository.save(session);
    }

    // 読書ペースの計算
    public double calculateAveragePages(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("本が見つかりません"));
        
        List<ReadingSession> sessions = sessionRepository.findByBookOrderByStartTimeDesc(book);
        
        return sessions.stream()
                .mapToInt(ReadingSession::getPagesRead)
                .average()
                .orElse(0.0);
    }

    // 読了予定日の予測
    public LocalDate predictCompletionDate(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("本が見つかりません"));
        
        double averagePages = calculateAveragePages(bookId);
        int remainingPages = book.getTotalPages() - book.getCurrentPage();
        
        if (averagePages <= 0) {
            return null;
        }
        
        long daysToComplete = (long) Math.ceil(remainingPages / averagePages);
        return LocalDate.now().plusDays(daysToComplete);
    }
    
    // 読書目標の設定
    public ReadingGoal setReadingGoal(Long bookId, LocalDate targetDate, Integer dailyPagesGoal) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("本が見つかりません"));
        
        // 既存の目標があれば更新、なければ新規作成
        ReadingGoal goal = goalRepository.findByBook(book)
                .orElse(new ReadingGoal());
        
        goal.setBook(book);
        goal.setTargetCompletionDate(targetDate);
        goal.setDailyPagesGoal(dailyPagesGoal);
        
        return goalRepository.save(goal);
    }

    // 読書目標の取得
    public ReadingGoal getReadingGoal(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("本が見つかりません"));
        
        return goalRepository.findByBook(book)
                .orElse(null);
    }

    // 目標達成度の計算
    public double calculateGoalProgress(Long bookId) {
        ReadingGoal goal = getReadingGoal(bookId);
        if (goal == null) {
            return 0.0;
        }

        Book book = goal.getBook();
        int currentPage = book.getCurrentPage();
        int totalPages = book.getTotalPages();
        
        return (double) currentPage / totalPages * 100;
    }
}
