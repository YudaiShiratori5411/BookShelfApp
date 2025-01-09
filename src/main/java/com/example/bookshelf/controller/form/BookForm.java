package com.example.bookshelf.controller.form;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class BookForm {
	
	private Long id;
	
    @Size(max = 23, message = "タイトルは23文字以内で入力してください")
    private String title;
    
    private String author;
    private String category;
    private String readingStatus;
    private Integer currentPage;
    
    @Pattern(regexp = "^\\d+$|^不明$", message = "ページ数は数字または「不明」を入力してください")
    private String totalPages;
    
    private String coverImage;
    private String memo;

    // デフォルトコンストラクタ
    public BookForm() {
        this.readingStatus = "NOT_STARTED";  // デフォルトを「未読」に設定
        this.currentPage = 0;  // デフォルトを0に設定
    }
    
    // getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getReadingStatus() {
        return readingStatus;
    }

    public void setReadingStatus(String readingStatus) {
        this.readingStatus = readingStatus;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public String getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(String totalPages) {
        this.totalPages = totalPages;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
}

