package com.example.bookshelf.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bookshelf.entity.User;
import com.example.bookshelf.repository.UserRepository;
import com.example.bookshelf.util.PasswordHashUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
   private final UserRepository userRepository;
   private final ShelfService shelfService;

   @Transactional
   public User registerUser(String username, String password) {
       if (userRepository.existsByUsername(username)) {
           throw new RuntimeException("このユーザー名は既に使用されています");
       }

       // ユーザーの作成
       User user = new User();
       user.setUsername(username);
       
       String salt = PasswordHashUtil.generateSalt();
       String hashedPassword = PasswordHashUtil.hashPassword(password, salt);
       
       user.setSalt(salt);
       user.setPassword(hashedPassword);

       user = userRepository.save(user);

       // 初期本棚の作成
       createDefaultShelves(user.getId());

       return user;
   }

   public User authenticateUser(String username, String password) {
       User user = userRepository.findByUsername(username);
       if (user == null) {
           return null;
       }

       String hashedPassword = PasswordHashUtil.hashPassword(password, user.getSalt());
       if (hashedPassword.equals(user.getPassword())) {
           return user;
       }

       return null;
   }

   private void createDefaultShelves(Long userId) {
       // デフォルトの本棚を作成
       String[] defaultShelves = {
           "技術書",
           "外国語",
           "小説",
           "マンガ",
           "趣味・実用書",
           "その他"
       };

       for (int i = 0; i < defaultShelves.length; i++) {
           shelfService.createShelf(defaultShelves[i], null, userId);
       }
   }

   // ユーザー存在確認
   public boolean existsById(Long userId) {
       return userRepository.existsById(userId);
   }

   // ユーザー取得
   public User findById(Long userId) {
       return userRepository.findById(userId)
           .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません: " + userId));
   }

   // ユーザー名によるユーザー取得
   public User findByUsername(String username) {
       return userRepository.findByUsername(username);
   }
}
