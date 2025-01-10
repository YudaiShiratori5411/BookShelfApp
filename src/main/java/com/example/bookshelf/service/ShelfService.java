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
    
    public List<Shelf> getAllShelves() {
        return shelfRepository.findAllOrderByPosition();
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
    
    @Transactional
    public Shelf createShelf(String name, String referenceShelfId) {
        // 新しい段の位置を決定
        Integer newPosition = calculateNewPosition(referenceShelfId);
        
        // 必要に応じて既存の段の位置を更新
        if (referenceShelfId != null) {
            shiftShelfPositions(newPosition);
        }

        // 新しい段を作成
        Shelf newShelf = new Shelf();
        newShelf.setName(name);
        newShelf.setPosition(newPosition);
        
        return shelfRepository.save(newShelf);
    }
    
    @Transactional
    public void deleteShelf(Long id) {
        Shelf shelf = shelfRepository.findByIdWithBooksAndDividers(id)
            .orElseThrow(() -> new RuntimeException("段が見つかりません: " + id));
        
        // この段にある本と仕切りの存在確認
        if (!shelf.getBooks().isEmpty()) {
            throw new RuntimeException("この段には本が登録されているため削除できません");
        }
        
        // 仕切りの削除
        shelf.getDividers().clear();
        
        // 段の削除
        shelfRepository.delete(shelf);
        
        // 後続の段のposition調整
        shelfRepository.decrementPositionsAfter(shelf.getPosition());
    }
    
    private Integer calculateNewPosition(String referenceShelfId) {
        if (referenceShelfId == null) {
            // 参照する段が指定されていない場合は最後に追加
            Integer maxPosition = shelfRepository.findMaxShelfPosition();
            return maxPosition != null ? maxPosition + 1 : 0;
        }

        // 参照する段の後ろに追加
        Shelf referenceShelf = shelfRepository.findById(Long.parseLong(referenceShelfId))
            .orElseThrow(() -> new IllegalArgumentException("Reference shelf not found: " + referenceShelfId));
            
        return referenceShelf.getPosition() + 1;
    }

    private void shiftShelfPositions(Integer fromPosition) {
        // updatePositionsのメソッド名を新しいものに変更
        shelfRepository.updateShelfPositions(fromPosition);
    }
    
    @Transactional
    public Shelf renameShelf(Long shelfId, String newName) {
        Shelf shelf = shelfRepository.findById(shelfId)
            .orElseThrow(() -> new IllegalArgumentException("指定された本棚が見つかりません"));
            
        shelf.setName(newName);
        return shelfRepository.save(shelf);
    }
}

