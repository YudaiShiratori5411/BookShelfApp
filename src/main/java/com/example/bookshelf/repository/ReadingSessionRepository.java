package com.example.bookshelf.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.bookshelf.entity.Book;
import com.example.bookshelf.entity.ReadingSession;

public interface ReadingSessionRepository extends JpaRepository<ReadingSession, Long> {
    // ページネーション付きで本とユーザーIDによる検索
    Page<ReadingSession> findByBookAndUserId(Book book, Long userId, Pageable pageable);
    
    // 本とユーザーIDによる検索（開始時間の降順）
    List<ReadingSession> findByBookAndUserIdOrderByStartTimeDesc(Book book, Long userId);
    
    // 期間指定での検索（ユーザーID付き）
    List<ReadingSession> findByBookAndUserIdAndStartTimeBetween(
        Book book, 
        Long userId, 
        LocalDateTime start, 
        LocalDateTime end
    );
    
    // IDとユーザーIDによる検索
    Optional<ReadingSession> findByIdAndUserId(Long id, Long userId);
    
    // ユーザーIDによる全セッション取得
    List<ReadingSession> findByUserId(Long userId);
    
    // 特定の本のセッション数をカウント
    long countByBookAndUserId(Book book, Long userId);
    
    // ユーザーIDと本による存在確認
    boolean existsByBookAndUserId(Book book, Long userId);
    
    // IDとユーザーIDによる削除
    void deleteByIdAndUserId(Long id, Long userId);
}
