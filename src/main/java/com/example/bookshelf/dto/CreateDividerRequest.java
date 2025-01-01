package com.example.bookshelf.dto;

import lombok.Data;

@Data
public class CreateDividerRequest {
    private Long shelfId;
    private String label;
    private Integer position;
}