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

    public Shelf getShelfById(Long id) {
        return shelfRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("本棚が見つかりません: " + id));
    }

    public void updateShelfOrder(Long id, Integer newOrder) {
        Shelf shelf = getShelfById(id);
        shelf.setPosition(newOrder);
        shelfRepository.save(shelf);
    }
    
    @Transactional(readOnly = true)
    public List<Shelf> getAllShelvesWithBooks() {
        List<Shelf> shelves = shelfRepository.findAllWithBooksAndDividersOrdered();
        
        // 各シェルフの本と仕切りの位置を確認
        shelves.forEach(shelf -> {
            System.out.println("Shelf: " + shelf.getName());
            System.out.println("Books:");
            shelf.getBooks().forEach(book -> 
                System.out.println(" - Book: " + book.getTitle() + " at position: " + book.getPosition())
            );
            System.out.println("Dividers:");
            shelf.getDividers().forEach(divider -> 
                System.out.println(" - Divider: " + divider.getLabel() + " at position: " + divider.getPosition())
            );
        });
        
        return shelves;
    }
}