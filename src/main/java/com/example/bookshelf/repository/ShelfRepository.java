package com.example.bookshelf.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.bookshelf.entity.Shelf;

public interface ShelfRepository extends JpaRepository<Shelf, Long> {
    Optional<Shelf> findByName(String name);
    
    @Query("SELECT DISTINCT s FROM Shelf s " +
    	    "LEFT JOIN FETCH s.books b " +
    	    "LEFT JOIN FETCH s.dividers d " +
    	    "WHERE s.id = :shelfId")
    Optional<Shelf> findByIdWithBooksAndDividers(@Param("shelfId") Long shelfId);

    @Query("SELECT DISTINCT s FROM Shelf s " +
    		"LEFT JOIN FETCH s.books b " +
    		"LEFT JOIN FETCH s.dividers d " +
    		"ORDER BY s.position ASC, b.position ASC, d.position ASC")
    List<Shelf> findAllWithBooksAndDividersOrdered();
}