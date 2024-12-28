package com.example.bookshelf.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "reading_goals")
@Data
public class ReadingGoal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "book_id")
    private Book book;
    
    @Column(name = "target_completion_date")
    private LocalDate targetCompletionDate;
    
    @Column(name = "daily_pages_goal")
    private Integer dailyPagesGoal;
}
