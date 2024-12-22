package com.example.bookshelf.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bookshelf.entity.Shelf;
import com.example.bookshelf.repository.ShelfRepository;

@Service
@Transactional
public class ShelfService {
    private final ShelfRepository shelfRepository;

    public ShelfService(ShelfRepository shelfRepository) {
        this.shelfRepository = shelfRepository;
    }

//    public List<Shelf> getAllShelves() {
//        return shelfRepository.findAllByOrderByDisplayOrderAsc();
//    }

    public Shelf getShelfById(Long id) {
        return shelfRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("本棚が見つかりません: " + id));
    }

    public void updateShelfOrder(Long id, Integer newOrder) {
        Shelf shelf = getShelfById(id);
        shelf.setDisplayOrder(newOrder);
        shelfRepository.save(shelf);
    }
    
    public List<Shelf> getAllShelvesWithBooks() {
        List<Shelf> shelves = shelfRepository.findAllByOrderByDisplayOrderAsc();
        shelves.forEach(shelf -> {
            System.out.println("Shelf: " + shelf.getName() + ", Books count: " + shelf.getBooks().size());
            shelf.getBooks().forEach(book -> {
                System.out.println(" - Book: " + book.getTitle());
            });
        });
        return shelves;
    }
}