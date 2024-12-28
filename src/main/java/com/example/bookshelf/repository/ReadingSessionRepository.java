package com.example.bookshelf.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.bookshelf.entity.Book;
import com.example.bookshelf.entity.ReadingSession;

public interface ReadingSessionRepository extends JpaRepository<ReadingSession, Long> {
    Page<ReadingSession> findByBook(Book book, Pageable pageable);
    List<ReadingSession> findByBookOrderByStartTimeDesc(Book book);
    List<ReadingSession> findByBookAndStartTimeBetween(Book book, LocalDateTime start, LocalDateTime end);
}
