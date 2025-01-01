package com.example.bookshelf.dto;

import com.example.bookshelf.entity.Divider;

import lombok.Data;

@Data
public class DividerResponseDto {
    private Long id;
    private String label;
    private Long shelfId;
    private Integer position;

    public static DividerResponseDto fromEntity(Divider divider) {
        DividerResponseDto dto = new DividerResponseDto();
        dto.setId(divider.getId());
        dto.setLabel(divider.getLabel());
        dto.setShelfId(divider.getShelf().getId());
        dto.setPosition(divider.getPosition());
        return dto;
    }
}