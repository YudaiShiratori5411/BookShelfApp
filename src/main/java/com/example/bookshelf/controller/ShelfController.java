package com.example.bookshelf.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bookshelf.dto.CreateShelfRequest;
import com.example.bookshelf.dto.RenameShelfRequest;
import com.example.bookshelf.entity.Shelf;
import com.example.bookshelf.service.ShelfService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/shelves")
@RequiredArgsConstructor
public class ShelfController {
    private final ShelfService shelfService;

    @PostMapping
    public ResponseEntity<?> createShelf(@RequestBody CreateShelfRequest request, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "ログインが必要です"));
        }

        try {
            Shelf newShelf = shelfService.createShelf(
                request.getName(), 
                request.getReferenceShelfId(),
                userId
            );
            return ResponseEntity.ok(newShelf);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteShelf(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "ログインが必要です"));
        }

        try {
            // 本棚の所有者チェック
            if (!shelfService.isShelfOwner(id, userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "この本棚を削除する権限がありません"));
            }

            shelfService.deleteShelf(id, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @PutMapping("/{id}/rename")
    public ResponseEntity<?> renameShelf(@PathVariable Long id, 
                                       @RequestBody RenameShelfRequest request, 
                                       HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "ログインが必要です"));
        }

        try {
            // 本棚の所有者チェック
            if (!shelfService.isShelfOwner(id, userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "この本棚を変更する権限がありません"));
            }

            Shelf renamedShelf = shelfService.renameShelf(id, request.getName(), userId);
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(renamedShelf);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("error", e.getMessage()));
        }
    }
}
