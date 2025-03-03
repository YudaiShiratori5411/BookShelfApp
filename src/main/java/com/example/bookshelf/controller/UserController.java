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
import com.example.bookshelf.util.PasswordHashUtil;

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
    
    @GetMapping("/api/user/{userId}")
    @ResponseBody
    public ResponseEntity<UserDto> getUserById(@PathVariable Long userId) {
        try {
            User user = userService.findById(userId);
            UserDto dto = new UserDto(user.getId(), user.getUsername(), user.getProfileImagePath());
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // ユーザー切り替えAPI
    @PostMapping("/switch/{userId}")
    @ResponseBody
    public ResponseEntity<?> switchUser(@PathVariable Long userId, HttpSession session) {
        try {
            User user = userService.findById(userId);
            // 現在のユーザー情報をセッションに保存
            session.setAttribute("userId", user.getId());
            session.setAttribute("currentUser", user);
            
            return ResponseEntity.ok().body(Map.of(
                "success", true,
                "username", user.getUsername(),
                "profileImagePath", user.getProfileImagePath()
            ));
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
            String imagePath = "/images/default-profile.jpg";
            
            if (profileImage != null && !profileImage.isEmpty()) {
                imagePath = saveProfileImage(profileImage);
            }
            
            // ユーザー登録（プロフィール画像パスも一緒に渡す）
            User user = userService.registerUser(username, password, imagePath);
            
            // セッションにユーザー情報を設定
            session.setAttribute("userId", user.getId());
            session.setAttribute("currentUser", user);
            
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
    
    @GetMapping("/profile/{userId}")
    public String showUserProfile(@PathVariable Long userId, Model model, HttpSession session) {
        User user = userService.findById(userId);
        model.addAttribute("user", user);
        
        // 現在のセッションユーザーを設定（ヘッダー表示用）
        if (session.getAttribute("currentUser") == null) {
            session.setAttribute("currentUser", user);
        }
        
        return "users/profile";
    }
    
    @GetMapping("/profile")
    public String showCurrentUserProfile(HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            // セッションからユーザーIDを取得
            Long userId = (Long) session.getAttribute("userId");
            if (userId != null) {
                currentUser = userService.findById(userId);
                session.setAttribute("currentUser", currentUser);
            } else {
                // デフォルトユーザーなど適切な処理
                redirectAttributes.addFlashAttribute("error", "ユーザー情報が見つかりません");
                return "redirect:/books";
            }
        }
        
        return "redirect:/users/profile/" + currentUser.getId();
    }
    
    @PostMapping("/profile/update")
    public String updateUserProfile(
            @RequestParam Long userId,
            @RequestParam String username,
            @RequestParam(required = false) String newPassword,
            @RequestParam(required = false) MultipartFile profileImage,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        
        try {
            User user = userService.findById(userId);
            
            // ユーザー名が変更され、既に存在する場合はエラー
            if (!user.getUsername().equals(username) && userService.existsByUsername(username)) {
                redirectAttributes.addFlashAttribute("error", "このユーザー名は既に使用されています");
                return "redirect:/users/profile/" + userId;
            }
            
            // ユーザー名を更新
            user.setUsername(username);
            
            // パスワードが入力されていれば更新
            if (newPassword != null && !newPassword.isEmpty()) {
                String salt = PasswordHashUtil.generateSalt();
                String hashedPassword = PasswordHashUtil.hashPassword(newPassword, salt);
                user.setSalt(salt);
                user.setPassword(hashedPassword);
            }
            
            // 画像更新フラグ
            boolean imageUpdated = false;
            String cacheBustedPath = null;
            
            // プロフィール画像が選択されていれば更新
            if (profileImage != null && !profileImage.isEmpty()) {
                String imagePath = saveProfileImage(profileImage);
                user.setProfileImagePath(imagePath);
                
                // タイムスタンプ付きの画像パス
                cacheBustedPath = imagePath + "?t=" + System.currentTimeMillis();
                imageUpdated = true;
            }
            
            // ユーザー情報を保存
            userService.updateUser(user);
            
            // セッションユーザー情報を更新
            session.setAttribute("currentUser", user);
            session.setAttribute("userId", user.getId());
            
            if (imageUpdated && cacheBustedPath != null) {
                session.setAttribute("updatedProfileImage", cacheBustedPath);
            }
            
            // リダイレクト属性の設定
            redirectAttributes.addFlashAttribute("success", "プロフィールを更新しました");
            redirectAttributes.addFlashAttribute("profileUpdated", true);
            redirectAttributes.addFlashAttribute("updatedUserId", user.getId());
            redirectAttributes.addFlashAttribute("updatedUserName", user.getUsername());
            
            if (imageUpdated && cacheBustedPath != null) {
                redirectAttributes.addFlashAttribute("updatedImagePath", cacheBustedPath);
                redirectAttributes.addFlashAttribute("forceImageUpdate", true);
            }
            
            // 強制的なUI更新のためのフラグ
            redirectAttributes.addFlashAttribute("forceUserReload", true);
            redirectAttributes.addFlashAttribute("userId", user.getId());
            redirectAttributes.addFlashAttribute("username", user.getUsername());
            
            // 画像パスを常にタイムスタンプ付きで提供
            String profileImagePath = user.getProfileImagePath() + "?t=" + System.currentTimeMillis();
            redirectAttributes.addFlashAttribute("profileImagePath", profileImagePath);
            
            return "redirect:/books?forceReload=" + System.currentTimeMillis();
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "プロフィール更新に失敗しました: " + e.getMessage());
            return "redirect:/users/profile/" + userId;
        }
    }
}