package com.example.bookshelf.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bookshelf.entity.Book;
import com.example.bookshelf.entity.ReadingSession;
import com.example.bookshelf.repository.ReadingSessionRepository;

@Service
public class ReadingSessionService {

    @Autowired
    private ReadingSessionRepository readingSessionRepository;

    @Autowired
    private BookService bookService;  // 本の情報を取得するために必要

    // セッションを保存
    @Transactional
    public ReadingSession saveSession(ReadingSession session) {
        return readingSessionRepository.save(session);
    }

    // 特定の本のすべてのセッションを取得（新しい順）
    public List<ReadingSession> getSessionsByBook(Book book) {
        return readingSessionRepository.findByBookOrderByStartTimeDesc(book);
    }

    // 特定の本の特定期間のセッションを取得
    public List<ReadingSession> getSessionsByBookAndPeriod(Book book, LocalDateTime start, LocalDateTime end) {
        return readingSessionRepository.findByBookAndStartTimeBetween(book, start, end);
    }

    // 1日の平均ページ数を計算
    public double calculateAveragePagesPerDay(Book book) {
        List<ReadingSession> sessions = getSessionsByBook(book);
        if (sessions.isEmpty()) {
            return 0.0;
        }

        int totalPages = sessions.stream()
                .mapToInt(ReadingSession::getPagesRead)
                .sum();

        // 最初のセッションと最後のセッションの日数差を計算
        LocalDateTime firstSession = sessions.get(sessions.size() - 1).getStartTime();
        LocalDateTime lastSession = sessions.get(0).getStartTime();
        
        // 日数の差が0の場合（同じ日のみ）は1日として扱う
        long days = Math.max(1, java.time.Duration.between(firstSession, lastSession).toDays() + 1);

        return (double) totalPages / days;
    }
    
 // 予測読了日を計算
    public LocalDateTime calculatePredictedFinishDate(Book book) {
        double avgPagesPerDay = calculateAveragePagesPerDay(book);
        if (avgPagesPerDay <= 0) {
            return LocalDateTime.now().plusMonths(1); // デフォルト値
        }

        // 残りのページ数を計算
        int totalPages = book.getTotalPages();
        int readPages = calculateTotalPagesRead(book);
        int remainingPages = totalPages - readPages;

        // 既に読了している場合
        if (remainingPages <= 0) {
            return LocalDateTime.now();
        }

        // 残りの日数を計算
        long daysToFinish = (long) Math.ceil(remainingPages / avgPagesPerDay);
        return LocalDateTime.now().plusDays(daysToFinish);
    }

    // 読んだ総ページ数を計算
    private int calculateTotalPagesRead(Book book) {
        List<ReadingSession> sessions = getSessionsByBook(book);
        return sessions.stream()
                .mapToInt(ReadingSession::getPagesRead)
                .sum();
    }
    
 // セッションの削除
    @Transactional
    public void deleteSession(Long sessionId) {
        readingSessionRepository.deleteById(sessionId);
    }

    // セッションの更新
    @Transactional
    public ReadingSession updateSession(Long sessionId, ReadingSession updatedSession) {
        ReadingSession existingSession = readingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        
        existingSession.setStartTime(updatedSession.getStartTime());
        existingSession.setEndTime(updatedSession.getEndTime());
        existingSession.setPagesRead(updatedSession.getPagesRead());
        
        return readingSessionRepository.save(existingSession);
    }
    
    // セッションをIDで取得するメソッド
    public Optional<ReadingSession> getSessionById(Long sessionId) {
        return readingSessionRepository.findById(sessionId);
    }
    
    // ページネーション付きでセッションを取得するメソッドを追加
    public Page<ReadingSession> getSessionsByBookPaged(Book book, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startTime"));
        return readingSessionRepository.findByBook(book, pageRequest);
    }
}