package com.example.bookshelf.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.bookshelf.entity.Book;
import com.example.bookshelf.entity.Book.ReadingStatus;

public interface BookRepository extends JpaRepository<Book, Long> {
    // カテゴリーで検索
    List<Book> findByCategory(String category);

    // 読書状態で検索
    List<Book> findByReadingStatus(ReadingStatus readingStatus);

    // タイトルで部分一致検索
    List<Book> findByTitleContaining(String title);

    // 著者で部分一致検索
    List<Book> findByAuthorContaining(String author);

    // カテゴリーと読書状態で検索
    List<Book> findByCategoryAndReadingStatus(String category, ReadingStatus readingStatus);

    // 進捗率で検索（指定したパーセンテージ以上の本を検索）
    @Query("SELECT b FROM Book b WHERE (b.currentPage * 100.0 / b.totalPages) >= :percentage")
    List<Book> findByReadingProgressGreaterThan(@Param("percentage") double percentage);

    // メモが登録されている本を検索
    List<Book> findByMemoIsNotNull();

    // 最近更新された本を取得（デフォルトで降順）
    List<Book> findTop10ByOrderByUpdatedAtDesc();

    // タイトルまたは著者で検索
    @Query("SELECT b FROM Book b WHERE b.title LIKE %:keyword% OR b.author LIKE %:keyword%")
    List<Book> searchByKeyword(@Param("keyword") String keyword);
    
    @Modifying
    @Query("UPDATE Book b SET b.position = :position WHERE b.id = :id")
    void updatePosition(@Param("id") Long id, @Param("position") Integer position);

    // タイトル、著者、カテゴリーで複合検索
    @Query("SELECT b FROM Book b WHERE " +
           "(:title IS NULL OR b.title LIKE %:title%) AND " +
           "(:author IS NULL OR b.author LIKE %:author%) AND " +
           "(:category IS NULL OR b.category = :category)")
    List<Book> searchBooks(
        @Param("title") String title,
        @Param("author") String author,
        @Param("category") String category
    );
    
    @Query("SELECT b FROM Book b WHERE b.shelf.id = :shelfId ORDER BY b.position")
    List<Book> findAllByShelf_IdOrderByPosition(@Param("shelfId") Long shelfId);
}

