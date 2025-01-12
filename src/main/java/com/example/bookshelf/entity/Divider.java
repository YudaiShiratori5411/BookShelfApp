package com.example.bookshelf.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "dividers", indexes = {
	    @Index(name = "idx_divider_user", columnList = "user_id"),
	    @Index(name = "idx_divider_position", columnList = "position")
	})
@EqualsAndHashCode(exclude = "shelf")
public class Divider implements Sortable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String label;
    
    @Column(name = "position", nullable = false)
    private Integer position;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "shelf_id", nullable = false)
    @ToString.Exclude
    private Shelf shelf;
    
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
}