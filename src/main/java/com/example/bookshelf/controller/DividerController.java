package com.example.bookshelf.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bookshelf.dto.CreateDividerRequest;
import com.example.bookshelf.dto.DividerResponseDto;
import com.example.bookshelf.dto.ReorderDividersRequest;
import com.example.bookshelf.dto.UpdateDividerRequest;
import com.example.bookshelf.entity.Divider;
import com.example.bookshelf.repository.DividerRepository;
import com.example.bookshelf.service.DividerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dividers")
@RequiredArgsConstructor
public class DividerController {

    private final DividerService dividerService;
    private final DividerRepository dividerRepository;

    @PostMapping
    public ResponseEntity<DividerResponseDto> createDivider(@RequestBody CreateDividerRequest request) {
        Divider divider = dividerService.createDivider(request.getShelfId(), request.getLabel(), request.getPosition());
        return ResponseEntity.ok(DividerResponseDto.fromEntity(divider));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DividerResponseDto> updateDivider(
            @PathVariable Long id,
            @RequestBody UpdateDividerRequest request) {
        Divider divider = dividerService.updateDivider(id, request.getLabel(), request.getPosition());
        return ResponseEntity.ok(DividerResponseDto.fromEntity(divider));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDivider(@PathVariable Long id) {
        dividerService.deleteDivider(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/reorder")
    public ResponseEntity<Map<String, Object>> reorderDividers(@RequestBody ReorderDividersRequest request) {
        try {
            System.out.println("Received reorder request: " + request);
            dividerService.reorderDividers(request.getShelfId(), request.getDividerPositions());

            // 更新後の位置を取得して返す
            List<Divider> updatedDividers = dividerRepository.findAllByShelf_Id(Long.parseLong(request.getShelfId()));
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Successfully reordered dividers");
            response.put("positions", updatedDividers.stream()
                .map(d -> Map.of(
                    "id", d.getId(),
                    "position", d.getPosition()
                ))
                .collect(Collectors.toList()));
                
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to save: " + e.getMessage()));
        }
    }
}

