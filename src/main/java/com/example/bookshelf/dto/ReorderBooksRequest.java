package com.example.bookshelf.dto;

import java.util.List;

import lombok.Data;

@Data
public class ReorderBooksRequest {
    private Long shelfId;
    private List<Long> bookIds;
}