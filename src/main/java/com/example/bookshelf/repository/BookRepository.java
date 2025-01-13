package com.example.bookshelf.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.bookshelf.entity.Book;
import com.example.bookshelf.entity.Book.ReadingStatus;

public interface BookRepository extends JpaRepository<Book, Long> {
    // 基本的なユーザー関連のクエリ
    List<Book> findByUserId(Long userId);
    Page<Book> findByUserId(Long userId, Pageable pageable);
    Optional<Book> findByIdAndUserId(Long id, Long userId);
    boolean existsByIdAndUserId(Long id, Long userId);
    void deleteByIdAndUserId(Long id, Long userId);

    // カテゴリーで検索
    List<Book> findByCategoryAndUserId(String category, Long userId);

    // 読書状態で検索
    List<Book> findByReadingStatusAndUserId(ReadingStatus readingStatus, Long userId);

    // タイトルで部分一致検索
    List<Book> findByTitleContainingAndUserId(String title, Long userId);

    // 著者で部分一致検索
    List<Book> findByAuthorContainingAndUserId(String author, Long userId);

    // カテゴリーと読書状態で検索
    List<Book> findByCategoryAndReadingStatusAndUserId(String category, ReadingStatus readingStatus, Long userId);

    // 進捗率で検索（ユーザーID付き）
    @Query("SELECT b FROM Book b WHERE (b.currentPage * 100.0 / b.totalPages) >= :percentage AND b.userId = :userId")
    List<Book> findByReadingProgressGreaterThanAndUserId(@Param("percentage") double percentage, @Param("userId") Long userId);

    // メモが登録されている本を検索
    List<Book> findByMemoIsNotNullAndUserId(Long userId);

    // 最近更新された本を取得
    List<Book> findTop10ByUserIdOrderByUpdatedAtDesc(Long userId);

    // タイトルまたは著者で検索
    @Query("SELECT b FROM Book b WHERE (b.title LIKE %:keyword% OR b.author LIKE %:keyword%) AND b.userId = :userId")
    List<Book> searchByKeywordAndUserId(@Param("keyword") String keyword, @Param("userId") Long userId);

    // 位置の更新
    @Modifying
    @Query("UPDATE Book b SET b.position = :position WHERE b.id = :id AND b.userId = :userId")
    void updatePositionForUser(@Param("id") Long id, @Param("position") Integer position, @Param("userId") Long userId);

    // タイトル、著者、カテゴリーで複合検索
    @Query("SELECT b FROM Book b WHERE " +
           "(:title IS NULL OR b.title LIKE %:title%) AND " +
           "(:author IS NULL OR b.author LIKE %:author%) AND " +
           "(:category IS NULL OR b.category = :category) AND " +
           "b.userId = :userId")
    List<Book> searchBooksByUserId(
        @Param("title") String title,
        @Param("author") String author,
        @Param("category") String category,
        @Param("userId") Long userId
    );

    // 本棚に関連する検索
    @Query("SELECT b FROM Book b WHERE b.shelf.id = :shelfId AND b.userId = :userId ORDER BY b.position")
    List<Book> findAllByShelf_IdAndUserIdOrderByPosition(@Param("shelfId") Long shelfId, @Param("userId") Long userId);

    // 最大position値の取得
    @Query("SELECT MAX(b.position) FROM Book b WHERE b.shelf.id = :shelfId AND b.userId = :userId")
    Integer findMaxPositionByShelfIdAndUserId(@Param("shelfId") Long shelfId, @Param("userId") Long userId);

    // 本の順序の一括更新
    @Modifying
    @Query("UPDATE Book b SET b.position = :position WHERE b.id = :id AND b.userId = :userId")
    void updateBookPositionForUser(@Param("id") Long id, @Param("position") Integer position, @Param("userId") Long userId);
    
    @Query("SELECT b FROM Book b WHERE b.shelf.id = :shelfId AND b.userId = :userId ORDER BY b.position ASC")
    List<Book> findAllByShelf_IdAndUserIdOrderByPositionAsc(
        @Param("shelfId") Long shelfId, 
        @Param("userId") Long userId
    );
}

