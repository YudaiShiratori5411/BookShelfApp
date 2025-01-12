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
    
    // ユーザーIDに基づいて本棚を取得
    public List<Shelf> getAllShelves(Long userId) {
        return shelfRepository.findAllByUserIdOrderByPosition(userId);
    }

    // 特定のユーザーの本棚を取得
    public Shelf getShelfById(Long id, Long userId) {
        Shelf shelf = shelfRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("本棚が見つかりません: " + id));
        
        if (!shelf.getUserId().equals(userId)) {
            throw new RuntimeException("この本棚にアクセスする権限がありません");
        }
        return shelf;
    }

    public void updateShelfOrder(Long id, Integer newOrder, Long userId) {
        Shelf shelf = getShelfById(id, userId);
        shelf.setPosition(newOrder);
        shelfRepository.save(shelf);
    }
    
    @Transactional(readOnly = true)
    public List<Shelf> getAllShelvesWithBooks(Long userId) {
        List<Shelf> shelves = shelfRepository.findAllWithBooksAndDividersOrderedByUserId(userId);
        
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
    public Shelf createShelf(String name, String referenceShelfId, Long userId) {
        // 新しい段の位置を決定
        Integer newPosition = calculateNewPosition(referenceShelfId, userId);
        
        // 必要に応じて既存の段の位置を更新
        if (referenceShelfId != null) {
            shiftShelfPositions(newPosition, userId);
        }

        // 新しい段を作成
        Shelf newShelf = new Shelf();
        newShelf.setName(name);
        newShelf.setPosition(newPosition);
        newShelf.setUserId(userId);  // ユーザーIDを設定
        
        return shelfRepository.save(newShelf);
    }
    
    @Transactional
    public void deleteShelf(Long id, Long userId) {
        Shelf shelf = shelfRepository.findByIdWithBooksAndDividers(id)
            .orElseThrow(() -> new RuntimeException("段が見つかりません: " + id));
        
        // 権限チェック
        if (!shelf.getUserId().equals(userId)) {
            throw new RuntimeException("この本棚を削除する権限がありません");
        }

        // この段にある本と仕切りの存在確認
        if (!shelf.getBooks().isEmpty()) {
            throw new RuntimeException("この段には本が登録されているため削除できません");
        }
        
        // 仕切りの削除
        shelf.getDividers().clear();
        
        // 段の削除
        shelfRepository.delete(shelf);
        
        // 後続の段のposition調整
        shelfRepository.decrementPositionsAfterForUser(shelf.getPosition(), userId);
    }
    
    private Integer calculateNewPosition(String referenceShelfId, Long userId) {
        if (referenceShelfId == null) {
            // 参照する段が指定されていない場合は最後に追加
            Integer maxPosition = shelfRepository.findMaxShelfPositionByUserId(userId);
            return maxPosition != null ? maxPosition + 1 : 0;
        }
        
        // 参照する段の後ろに追加
        Shelf referenceShelf = shelfRepository.findById(Long.parseLong(referenceShelfId))
            .orElseThrow(() -> new IllegalArgumentException("Reference shelf not found: " + referenceShelfId));
            
        // 権限チェック
        if (!referenceShelf.getUserId().equals(userId)) {
            throw new RuntimeException("この本棚にアクセスする権限がありません");
        }
            
        return referenceShelf.getPosition() + 1;
    }

    private void shiftShelfPositions(Integer fromPosition, Long userId) {
        shelfRepository.updateShelfPositionsForUser(fromPosition, userId);
    }
    
    @Transactional
    public Shelf renameShelf(Long shelfId, String newName, Long userId) {
        Shelf shelf = shelfRepository.findById(shelfId)
            .orElseThrow(() -> new IllegalArgumentException("指定された本棚が見つかりません"));
            
        // 権限チェック
        if (!shelf.getUserId().equals(userId)) {
            throw new RuntimeException("この本棚を変更する権限がありません");
        }
            
        shelf.setName(newName);
        return shelfRepository.save(shelf);
    }
    
    /**
     * 指定された本棚が指定されたユーザーのものかを確認する
     * @param shelfId 本棚のID
     * @param userId ユーザーID
     * @return 指定されたユーザーが本棚の所有者である場合はtrue
     */
    @Transactional(readOnly = true)
    public boolean isShelfOwner(Long shelfId, Long userId) {
        try {
            return shelfRepository.findById(shelfId)
                .map(shelf -> shelf.getUserId().equals(userId))
                .orElse(false);
        } catch (Exception e) {
            // エラーが発生した場合はfalseを返す（セキュリティ上の理由）
            return false;
        }
    }
}

