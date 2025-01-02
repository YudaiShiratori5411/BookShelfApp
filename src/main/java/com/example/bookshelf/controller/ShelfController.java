package com.example.bookshelf.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bookshelf.dto.CreateShelfRequest;
import com.example.bookshelf.entity.Shelf;
import com.example.bookshelf.service.ShelfService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/shelves")
@RequiredArgsConstructor
public class ShelfController {
    private final ShelfService shelfService;

    @PostMapping
    public ResponseEntity<Shelf> createShelf(@RequestBody CreateShelfRequest request) {
        // nameとreferenceShelfIdを渡すように修正
        Shelf newShelf = shelfService.createShelf(
            request.getName(), 
            request.getReferenceShelfId()
        );
        return ResponseEntity.ok(newShelf);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteShelf(@PathVariable Long id) {
        try {
            shelfService.deleteShelf(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
}
