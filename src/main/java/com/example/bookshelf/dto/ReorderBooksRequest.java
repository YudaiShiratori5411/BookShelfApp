package com.example.bookshelf.dto;

import java.util.List;

import lombok.Data;

@Data
public class ReorderBooksRequest {
    private String shelfId;
    private List<BookPosition> bookPositions;

    @Data
    public static class BookPosition {
        private String id;
        private Integer position;
    }
}