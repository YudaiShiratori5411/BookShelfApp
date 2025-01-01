package com.example.bookshelf.dto;

import lombok.Data;

@Data
public class UpdateDividerRequest {
    private String label;
    private Integer position;
}