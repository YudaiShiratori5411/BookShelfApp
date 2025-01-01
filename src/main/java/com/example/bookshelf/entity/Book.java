package com.example.bookshelf.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "books")
@Data
@EqualsAndHashCode(exclude = "shelf")
public class Book implements Sortable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(name = "total_pages")
    private Integer totalPages;

    @Column(name = "current_page")
    private Integer currentPage;

    @Column(nullable = false)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Column(name = "reading_status")
    @Enumerated(EnumType.STRING)
    private ReadingStatus readingStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
//    @Column(name = "display_order")
//    private Integer displayOrder;
    
    @Column(name = "position")
    private Integer position;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public Integer getPosition() {
        return position;
    }

    @Override
    public void setPosition(Integer position) {
        this.position = position;
    }
    
    
    
    
    
    
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum ReadingStatus {
        NOT_STARTED("未読"),
        READING("読書中"),
        COMPLETED("完読"),
        ON_HOLD("中断");

        private final String displayName;

        ReadingStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
    
    @ManyToOne
    @JoinColumn(name = "shelf_id")
    @ToString.Exclude
    private Shelf shelf;
    
    public void setShelf(Shelf shelf) {
        this.shelf = shelf;
    }
}

