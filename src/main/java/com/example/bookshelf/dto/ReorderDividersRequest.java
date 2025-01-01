package com.example.bookshelf.dto;

import java.util.List;

import lombok.Data;

@Data
public class ReorderDividersRequest {
    private String shelfId;
    private List<DividerPosition> dividerPositions;

    @Data
    public static class DividerPosition {
        private String id;
        private Integer position;
    }
}
