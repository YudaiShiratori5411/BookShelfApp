package com.example.bookshelf.dto;

import lombok.Data;

@Data
public class CreateShelfRequest {
    private String name;
    private String referenceShelfId;  // 基準となる段のID（この段の後に新しい段を追加）
}
