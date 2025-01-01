package com.example.bookshelf.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.bookshelf.entity.Divider;

public interface DividerRepository extends JpaRepository<Divider, Long> {
    @Query("SELECT d FROM Divider d WHERE d.shelf.id = :shelfId ORDER BY d.position")
    List<Divider> findAllByShelf_Id(@Param("shelfId") Long shelfId);
    
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Divider d SET d.position = :position WHERE d.id = :id")
    void updatePosition(@Param("id") Long id, @Param("position") Integer position);
    
    @Query("SELECT COALESCE(MAX(d.position), -1) FROM Divider d WHERE d.shelf.id = :shelfId")
    Integer findMaxPositionByShelfId(@Param("shelfId") Long shelfId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Divider d SET d.position = d.position + 1 WHERE d.shelf.id = :shelfId AND d.position >= :startPosition")
    void shiftPositionsUp(@Param("shelfId") Long shelfId, @Param("startPosition") Integer startPosition);

    @Query("SELECT d FROM Divider d WHERE d.shelf.id = :shelfId ORDER BY d.position ASC")
    List<Divider> findAllByShelf_IdOrderByPositionAsc(@Param("shelfId") Long shelfId);

    @Query("SELECT d FROM Divider d JOIN FETCH d.shelf WHERE d.shelf.id = :shelfId ORDER BY d.position")
    List<Divider> findAllWithShelfByShelfId(@Param("shelfId") Long shelfId);
}