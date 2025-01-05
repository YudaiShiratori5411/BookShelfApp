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
    public Divider createDivider(Long shelfId, String label, Integer position) {
        Shelf shelf = shelfRepository.findById(shelfId)
                .orElseThrow(() -> new IllegalArgumentException("Shelf not found: " + shelfId));

        // 現在の本棚内の全てのアイテムを取得して最大のposition値を見つける
        Integer maxBookPosition = bookRepository.findMaxPositionByShelfId(shelfId);
        if (maxBookPosition == null) maxBookPosition = -1;
        
        Integer maxDividerPosition = dividerRepository.findMaxPositionByShelfId(shelfId);
        if (maxDividerPosition == null) maxDividerPosition = -1;

        // 最大のposition値を使用
        Integer maxPosition = Math.max(maxBookPosition, maxDividerPosition);
        
        // 新しい仕切りを作成
        Divider divider = new Divider();
        divider.setShelf(shelf);
        divider.setLabel(label);
        divider.setPosition(maxPosition + 1);  // 最大値の次の値を使用

        return dividerRepository.save(divider);
    }

    @Transactional
    public Divider updateDivider(Long id, String label, Integer position) {
        Divider divider = dividerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Divider not found: " + id));

        divider.setLabel(label);
        divider.setPosition(position);

        return dividerRepository.save(divider);
    }

    @Transactional
    public void deleteDivider(Long id) {
        Divider divider = dividerRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Divider not found: " + id));
        
        // 関連するShelfを取得
        Shelf shelf = divider.getShelf();
        
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
    public void reorderDividers(String shelfId, List<ReorderDividersRequest.DividerPosition> positions) {
        Long shelfIdLong = Long.parseLong(shelfId);
        
        // 一時的な位置を使用（競合を避けるため）
        int offset = 10000;
        positions.forEach(pos -> {
            Long dividerId = Long.parseLong(pos.getId());
            dividerRepository.updatePosition(dividerId, offset + pos.getPosition());
        });
        dividerRepository.flush();

        // 位置を最終的な値に更新
        positions.forEach(pos -> {
            Long dividerId = Long.parseLong(pos.getId());
            dividerRepository.updatePosition(dividerId, pos.getPosition());
        });
        
        dividerRepository.flush();
    }
    
}