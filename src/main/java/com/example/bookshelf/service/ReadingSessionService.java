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
    private BookService bookService;

    // セッションを保存
    @Transactional
    public ReadingSession saveSession(ReadingSession session) {
        session.setUserId(session.getBook().getUserId());
        return readingSessionRepository.save(session);
    }

    // 特定の本のすべてのセッションを取得（新しい順）
    public List<ReadingSession> getSessionsByBook(Book book) {
        return readingSessionRepository.findByBookAndUserIdOrderByStartTimeDesc(book, book.getUserId());
    }

    // 特定の本の特定期間のセッションを取得
    public List<ReadingSession> getSessionsByBookAndPeriod(Book book, LocalDateTime start, LocalDateTime end) {
        return readingSessionRepository.findByBookAndUserIdAndStartTimeBetween(
            book, book.getUserId(), start, end);
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

        LocalDateTime firstSession = sessions.get(sessions.size() - 1).getStartTime();
        LocalDateTime lastSession = sessions.get(0).getStartTime();

        long days = Math.max(1, java.time.Duration.between(firstSession, lastSession).toDays() + 1);
        return (double) totalPages / days;
    }

    // 予測読了日を計算
    public LocalDateTime calculatePredictedFinishDate(Book book) {
        double avgPagesPerDay = calculateAveragePagesPerDay(book);
        if (avgPagesPerDay <= 0) {
            return LocalDateTime.now().plusMonths(1);
        }

        int totalPages = book.getTotalPages();
        int readPages = calculateTotalPagesRead(book);
        int remainingPages = totalPages - readPages;

        if (remainingPages <= 0) {
            return LocalDateTime.now();
        }

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

    // セッションの削除（userId無し）
    @Transactional
    public void deleteSession(Long sessionId) {
        ReadingSession session = readingSessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        readingSessionRepository.delete(session);
    }

    // セッションの削除（userId付き）
    @Transactional
    public void deleteSession(Long sessionId, Long userId) {
        ReadingSession session = readingSessionRepository.findByIdAndUserId(sessionId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Session not found or unauthorized"));
        readingSessionRepository.delete(session);
    }

    // セッションの更新（userId無し）
    @Transactional
    public ReadingSession updateSession(Long sessionId, ReadingSession updatedSession) {
        ReadingSession existingSession = readingSessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        existingSession.setStartTime(updatedSession.getStartTime());
        existingSession.setEndTime(updatedSession.getEndTime());
        existingSession.setPagesRead(updatedSession.getPagesRead());

        return readingSessionRepository.save(existingSession);
    }

    // セッションの更新（userId付き）
    @Transactional
    public ReadingSession updateSession(Long sessionId, ReadingSession updatedSession, Long userId) {
        ReadingSession existingSession = readingSessionRepository.findByIdAndUserId(sessionId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Session not found or unauthorized"));

        existingSession.setStartTime(updatedSession.getStartTime());
        existingSession.setEndTime(updatedSession.getEndTime());
        existingSession.setPagesRead(updatedSession.getPagesRead());

        return readingSessionRepository.save(existingSession);
    }

    // セッションをIDで取得（userId無し）
    public Optional<ReadingSession> getSessionById(Long sessionId) {
        return readingSessionRepository.findById(sessionId);
    }

    // セッションをIDで取得（userId付き）
    public Optional<ReadingSession> getSessionById(Long sessionId, Long userId) {
        return readingSessionRepository.findByIdAndUserId(sessionId, userId);
    }

    // ページネーション付きでセッションを取得
    public Page<ReadingSession> getSessionsByBookPaged(Book book, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startTime"));
        return readingSessionRepository.findByBookAndUserId(book, book.getUserId(), pageRequest);
    }
}


