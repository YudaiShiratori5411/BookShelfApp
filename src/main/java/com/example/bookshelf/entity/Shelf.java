package com.example.bookshelf.entity;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "shelves")
@Data
@EqualsAndHashCode(exclude = {"books", "dividers"})
public class Shelf implements Sortable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "position")
    private Integer position;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "shelf", fetch = FetchType.LAZY)
    @OrderBy("position ASC")
    @ToString.Exclude
    private Set<Book> books = new LinkedHashSet<>();

    @OneToMany(mappedBy = "shelf", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("position ASC")
    @ToString.Exclude
    private Set<Divider> dividers = new LinkedHashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
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
    
    @JsonIgnore
    public List<?> getMixedItems() {
        // null安全なComparatorを作成
        Comparator<Object> positionComparator = Comparator.comparing(
            item -> ((Sortable)item).getPosition(),
            Comparator.nullsLast(Comparator.naturalOrder())
        );

        // 本と仕切りを位置情報に基づいて統合
        List<Object> sortedItems = Stream.concat(
                books != null ? books.stream() : Stream.empty(),
                dividers != null ? dividers.stream() : Stream.empty()
            )
            .sorted(positionComparator)
            .collect(Collectors.toList());
        
        // 重複を避けて新しい位置を割り当て
        int currentPosition = 0;
        for (Object item : sortedItems) {
            if (item instanceof Sortable) {
                Sortable sortable = (Sortable) item;
                sortable.setPosition(currentPosition++);
            }
        }
        
        return sortedItems;
    }
}


