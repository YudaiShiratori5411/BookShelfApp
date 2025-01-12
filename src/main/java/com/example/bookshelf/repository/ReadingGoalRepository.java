package com.example.bookshelf.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.bookshelf.entity.Book;
import com.example.bookshelf.entity.ReadingGoal;

public interface ReadingGoalRepository extends JpaRepository<ReadingGoal, Long> {
    // 本とユーザーIDによる検索
    Optional<ReadingGoal> findByBookAndUserId(Book book, Long userId);
    
    // ユーザーIDによる検索
    List<ReadingGoal> findByUserId(Long userId);
    
    // 本とユーザーIDによる存在確認
    boolean existsByBookAndUserId(Book book, Long userId);
    
    // IDとユーザーIDによる検索
    Optional<ReadingGoal> findByIdAndUserId(Long id, Long userId);
    
    // IDとユーザーIDによる削除
    void deleteByIdAndUserId(Long id, Long userId);
}
