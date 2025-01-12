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
import com.example.bookshelf.service.DividerService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dividers")
@RequiredArgsConstructor
public class DividerController {
    private final DividerService dividerService;

    @PostMapping
    public ResponseEntity<?> createDivider(@RequestBody CreateDividerRequest request, 
                                         HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "ログインが必要です"));
        }

        try {
            Divider divider = dividerService.createDivider(
                request.getShelfId(), 
                request.getLabel(), 
                request.getPosition(),
                userId
            );
            return ResponseEntity.ok(DividerResponseDto.fromEntity(divider));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDivider(@PathVariable Long id,
                                         @RequestBody UpdateDividerRequest request,
                                         HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "ログインが必要です"));
        }

        try {
            Divider divider = dividerService.updateDivider(id, 
                                                         request.getLabel(), 
                                                         request.getPosition(),
                                                         userId);
            return ResponseEntity.ok(DividerResponseDto.fromEntity(divider));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDivider(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "ログインが必要です"));
        }

        try {
            dividerService.deleteDivider(id, userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/reorder")
    public ResponseEntity<?> reorderDividers(@RequestBody ReorderDividersRequest request,
                                           HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "ログインが必要です"));
        }

        try {
            dividerService.reorderDividers(request.getShelfId(), 
                                         request.getDividerPositions(),
                                         userId);
            
            List<Divider> updatedDividers = dividerService.getDividersByShelfId(
                Long.parseLong(request.getShelfId()), 
                userId
            );
            
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to save: " + e.getMessage()));
        }
    }
}

