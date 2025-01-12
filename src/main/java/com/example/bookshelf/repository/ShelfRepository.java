package com.example.bookshelf.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.bookshelf.entity.Shelf;

public interface ShelfRepository extends JpaRepository<Shelf, Long> {
	
    // ユーザーIDに基づく本棚の取得
    @Query("SELECT s FROM Shelf s WHERE s.id = :id AND s.userId = :userId")
    Optional<Shelf> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
    
    // 名前とユーザーIDに基づく本棚の取得
    @Query("SELECT s FROM Shelf s WHERE s.name = :name AND s.userId = :userId")
    Optional<Shelf> findByNameAndUserId(@Param("name") String name, @Param("userId") Long userId);
    
    // ユーザーIDに基づく本棚の取得（位置順）
    @Query("SELECT s FROM Shelf s WHERE s.userId = :userId ORDER BY s.position")
    List<Shelf> findAllByUserIdOrderByPosition(@Param("userId") Long userId);
    
    // 特定のユーザーの本と仕切りを含む本棚を取得
    @Query("SELECT DISTINCT s FROM Shelf s " +
           "LEFT JOIN FETCH s.books b " +
           "LEFT JOIN FETCH s.dividers d " +
           "WHERE s.userId = :userId " +
           "ORDER BY s.position, b.position, d.position")
    List<Shelf> findAllWithBooksAndDividersOrderedByUserId(@Param("userId") Long userId);
    
    // IDによる本棚の取得（本と仕切りを含む）
    @Query("SELECT DISTINCT s FROM Shelf s " +
           "LEFT JOIN FETCH s.books b " +
           "LEFT JOIN FETCH s.dividers d " +
           "WHERE s.id = :id")
    Optional<Shelf> findByIdWithBooksAndDividers(@Param("id") Long id);
    
    // 特定のユーザーの最大position値を取得
    @Query("SELECT MAX(s.position) FROM Shelf s WHERE s.userId = :userId")
    Integer findMaxShelfPositionByUserId(@Param("userId") Long userId);
    
    // 特定のユーザーの本棚の位置を更新（指定位置以降を1つずつ後ろにずらす）
    @Modifying
    @Query("UPDATE Shelf s SET s.position = s.position + 1 " +
           "WHERE s.position >= :position AND s.userId = :userId")
    void updateShelfPositionsForUser(@Param("position") Integer position, @Param("userId") Long userId);
    
    // 特定のユーザーの本棚の位置を更新（指定位置以降を1つずつ前にずらす）
    @Modifying
    @Query("UPDATE Shelf s SET s.position = s.position - 1 " +
           "WHERE s.position > :position AND s.userId = :userId")
    void decrementPositionsAfterForUser(@Param("position") Integer position, @Param("userId") Long userId);
    
    // 特定のユーザーの本棚が存在するかチェック
    boolean existsByIdAndUserId(Long id, Long userId);
    
    // 特定のユーザーの本棚を名前で検索
    List<Shelf> findByUserIdAndNameContainingOrderByPosition(Long userId, String name);
    
    // 特定のユーザーの特定の位置の本棚を取得
    Optional<Shelf> findByUserIdAndPosition(Long userId, Integer position);
    
    // 特定のユーザーの本棚数を取得
    @Query("SELECT COUNT(s) FROM Shelf s WHERE s.userId = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    // 特定のユーザーの特定の本棚のIDを検証
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Shelf s " +
           "WHERE s.id = :shelfId AND s.userId = :userId")
    boolean validateShelfBelongsToUser(@Param("shelfId") Long shelfId, @Param("userId") Long userId);
}

