package com.example.bookshelf.controller;

import org.springframework.http.ResponseEntity;
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
}
