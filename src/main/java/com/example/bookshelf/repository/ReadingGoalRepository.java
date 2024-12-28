package com.example.bookshelf.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.bookshelf.entity.Book;
import com.example.bookshelf.entity.ReadingGoal;

public interface ReadingGoalRepository extends JpaRepository<ReadingGoal, Long> {
    Optional<ReadingGoal> findByBook(Book book);
}
