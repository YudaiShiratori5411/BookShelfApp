package com.example.bookshelf.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.bookshelf.entity.Divider;

public interface DividerRepository extends JpaRepository<Divider, Long> {
    // 基本的なユーザー関連のクエリ
    List<Divider> findByUserId(Long userId);
    Optional<Divider> findByIdAndUserId(Long id, Long userId);
    boolean existsByIdAndUserId(Long id, Long userId);
    void deleteByIdAndUserId(Long id, Long userId);

    // 本棚に関連するクエリ（ユーザーID付き）
    @Query("SELECT d FROM Divider d WHERE d.shelf.id = :shelfId AND d.userId = :userId ORDER BY d.position")
    List<Divider> findAllByShelf_IdAndUserId(@Param("shelfId") Long shelfId, @Param("userId") Long userId);
    
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Divider d SET d.position = :position WHERE d.id = :id AND d.userId = :userId")
    void updatePositionForUser(@Param("id") Long id, @Param("position") Integer position, @Param("userId") Long userId);
    
    @Query("SELECT COALESCE(MAX(d.position), -1) FROM Divider d WHERE d.shelf.id = :shelfId AND d.userId = :userId")
    Integer findMaxPositionByShelfIdAndUserId(@Param("shelfId") Long shelfId, @Param("userId") Long userId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Divider d SET d.position = d.position + 1 " +
           "WHERE d.shelf.id = :shelfId AND d.userId = :userId AND d.position >= :startPosition")
    void shiftPositionsUpForUser(@Param("shelfId") Long shelfId, 
                                @Param("startPosition") Integer startPosition,
                                @Param("userId") Long userId);

    @Query("SELECT d FROM Divider d WHERE d.shelf.id = :shelfId AND d.userId = :userId ORDER BY d.position ASC")
    List<Divider> findAllByShelf_IdAndUserIdOrderByPositionAsc(@Param("shelfId") Long shelfId, 
                                                              @Param("userId") Long userId);

    @Query("SELECT d FROM Divider d JOIN FETCH d.shelf " +
           "WHERE d.shelf.id = :shelfId AND d.userId = :userId ORDER BY d.position")
    List<Divider> findAllWithShelfByShelfIdAndUserId(@Param("shelfId") Long shelfId, @Param("userId") Long userId);
}