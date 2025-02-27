package com.example.bookshelf.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.bookshelf.dto.UserDto;
import com.example.bookshelf.entity.User;
import com.example.bookshelf.service.UserService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    // ユーザー一覧取得API（ドロップダウン用）
    @GetMapping("/api/list")
    @ResponseBody
    public List<UserDto> getUserList() {
        return userService.getAllUsers().stream()
                .map(user -> new UserDto(user.getId(), user.getUsername(), user.getProfileImagePath()))
                .collect(Collectors.toList());
    }
    
    // ユーザー切り替えAPI
    @PostMapping("/switch/{userId}")
    @ResponseBody
    public ResponseEntity<?> switchUser(@PathVariable Long userId, HttpSession session) {
        try {
            User user = userService.findById(userId);
            session.setAttribute("userId", user.getId());
            return ResponseEntity.ok().body(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // ユーザー登録ページ表示
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        return "users/register";
    }
    
    // ユーザー登録処理
    @PostMapping("/register")
    public String registerUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(required = false) MultipartFile profileImage,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        try {
            // プロフィール画像の保存
            String imagePath = "/images/default-profile.png";
            
            if (profileImage != null && !profileImage.isEmpty()) {
                imagePath = saveProfileImage(profileImage);
            }
            
            // ユーザー登録（プロフィール画像パスも一緒に渡す）
            User user = userService.registerUser(username, password, imagePath);
            
            // セッションにユーザーIDを設定
            session.setAttribute("userId", user.getId());
            
            // ローカルストレージに保存するためのデータを渡す
            redirectAttributes.addFlashAttribute("newUserId", user.getId());
            redirectAttributes.addFlashAttribute("newUserName", user.getUsername());
            redirectAttributes.addFlashAttribute("newUserImagePath", user.getProfileImagePath());
            
            redirectAttributes.addFlashAttribute("success", "ユーザー登録が完了しました");
            return "redirect:/books";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "ユーザー登録に失敗しました: " + e.getMessage());
            return "redirect:/users/register";
        }
    }
    
    // プロフィール画像保存処理
    private String saveProfileImage(MultipartFile file) throws IOException {
        // 保存ディレクトリ
        String uploadDir = "uploads/profiles";
        Path uploadPath = Paths.get(uploadDir);
        
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // ユニークなファイル名を生成
        String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(uniqueFileName);
        
        // ファイル保存
        Files.copy(file.getInputStream(), filePath);
        
        return "/uploads/profiles/" + uniqueFileName;
    }
}