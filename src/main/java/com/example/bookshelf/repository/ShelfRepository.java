package com.example.bookshelf.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
    
    @Query("SELECT MAX(s.position) FROM Shelf s")
    Integer findMaxShelfPosition();

    @Modifying
    @Query("UPDATE Shelf s SET s.position = s.position + 1 WHERE s.position >= :fromPosition")
    void updateShelfPositions(@Param("fromPosition") Integer fromPosition);
    
    // position順でソートされた全ての本棚を取得するメソッド
    @Query("SELECT s FROM Shelf s ORDER BY s.position ASC")
    List<Shelf> findAllOrderByPosition();
    
    @Modifying
    @Query("UPDATE Shelf s SET s.position = s.position - 1 WHERE s.position > :position")
    void decrementPositionsAfter(@Param("position") Integer position);
}