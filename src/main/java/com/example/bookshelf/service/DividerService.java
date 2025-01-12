package com.example.bookshelf.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bookshelf.dto.ReorderDividersRequest;
import com.example.bookshelf.entity.Divider;
import com.example.bookshelf.entity.Shelf;
import com.example.bookshelf.repository.BookRepository;
import com.example.bookshelf.repository.DividerRepository;
import com.example.bookshelf.repository.ShelfRepository;

import lombok.RequiredArgsConstructor;

@Service("shelfDividerService")
@RequiredArgsConstructor
public class DividerService {
    private final DividerRepository dividerRepository;
    private final ShelfRepository shelfRepository;
    private final BookRepository bookRepository;

    @Transactional
    public Divider createDivider(Long shelfId, String label, Integer position, Long userId) {
        Shelf shelf = shelfRepository.findByIdAndUserId(shelfId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Shelf not found: " + shelfId));

        // 現在の本棚内の全てのアイテムを取得して最大のposition値を見つける
        Integer maxBookPosition = bookRepository.findMaxPositionByShelfIdAndUserId(shelfId, userId);
        if (maxBookPosition == null) maxBookPosition = -1;
        
        Integer maxDividerPosition = dividerRepository.findMaxPositionByShelfIdAndUserId(shelfId, userId);
        if (maxDividerPosition == null) maxDividerPosition = -1;
        
        // 最大のposition値を使用
        Integer maxPosition = Math.max(maxBookPosition, maxDividerPosition);
        
        // 新しい仕切りを作成
        Divider divider = new Divider();
        divider.setShelf(shelf);
        divider.setLabel(label);
        divider.setPosition(maxPosition + 1);
        divider.setUserId(userId);  // ユーザーIDを設定
        
        return dividerRepository.save(divider);
    }

    @Transactional
    public Divider updateDivider(Long id, String label, Integer position, Long userId) {
        Divider divider = dividerRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Divider not found: " + id));

        divider.setLabel(label);
        divider.setPosition(position);
        return dividerRepository.save(divider);
    }

    @Transactional
    public void deleteDivider(Long id, Long userId) {
        Divider divider = dividerRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new IllegalArgumentException("Divider not found: " + id));
        
        // 関連するShelfを取得し、所有者チェック
        Shelf shelf = divider.getShelf();
        if (!shelf.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to shelf");
        }
        
        // Shelfの仕切りリストから削除
        shelf.getDividers().remove(divider);
        
        // データベースから削除
        dividerRepository.delete(divider);
        
        // 変更を即時反映
        dividerRepository.flush();
        
        // 削除の確認
        System.out.println("Divider " + id + " deleted, exists: " + dividerRepository.existsById(id));
    }
    
    @Transactional
    public void reorderDividers(String shelfId, List<ReorderDividersRequest.DividerPosition> positions, Long userId) {
        Long shelfIdLong = Long.parseLong(shelfId);
        Shelf shelf = shelfRepository.findByIdAndUserId(shelfIdLong, userId)
            .orElseThrow(() -> new IllegalArgumentException("Shelf not found: " + shelfId));
        
        // 一時的な位置を使用
        int offset = 10000;
        positions.forEach(pos -> {
            Long dividerId = Long.parseLong(pos.getId());
            Divider divider = dividerRepository.findByIdAndUserId(dividerId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Divider not found: " + dividerId));
            
            // 本棚が変更された場合は更新
            if (!divider.getShelf().getId().equals(shelfIdLong)) {
                divider.setShelf(shelf);
            }
            divider.setPosition(offset + pos.getPosition());
            dividerRepository.save(divider);
        });
        dividerRepository.flush();

        // 最終的な位置に更新
        positions.forEach(pos -> {
            Long dividerId = Long.parseLong(pos.getId());
            dividerRepository.updatePositionForUser(dividerId, pos.getPosition(), userId);
        });
        
        dividerRepository.flush();
    }

    // 本棚に属する仕切りの取得
    @Transactional(readOnly = true)
    public List<Divider> getDividersByShelfId(Long shelfId, Long userId) {
        return dividerRepository.findAllByShelf_IdAndUserId(shelfId, userId);
    }

    // 仕切りの所有者チェック
    @Transactional(readOnly = true)
    public boolean isDividerOwner(Long dividerId, Long userId) {
        return dividerRepository.existsByIdAndUserId(dividerId, userId);
    }
}

