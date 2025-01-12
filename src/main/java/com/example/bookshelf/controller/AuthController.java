package com.example.bookshelf.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.bookshelf.entity.User;
import com.example.bookshelf.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;
    
    // ページ表示（GET）
    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("isLoginPage", true);
        return "login";
    }

    // ログイン処理（POST）
    @PostMapping("/login")
    public String login(@RequestParam String username,
                       @RequestParam String password,
                       HttpSession session,
                       Model model) {
        try {
            User user = userService.authenticateUser(username, password);
            if (user != null) {
                session.setAttribute("userId", user.getId());
                session.setAttribute("username", user.getUsername());
                return "redirect:/books";
            } else {
                model.addAttribute("error", "ユーザー名またはパスワードが正しくありません");
                return "login";
            }
        } catch (Exception e) {
            model.addAttribute("error", "ログイン処理中にエラーが発生しました");
            return "login";
        }
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                         @RequestParam String password,
                         @RequestParam String confirmPassword,
                         Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "パスワードが一致しません");
            return "login";
        }

        try {
            userService.registerUser(username, password);
            model.addAttribute("success", "登録が完了しました。ログインしてください。");
            return "login";
        } catch (RuntimeException e) {
            model.addAttribute("error", "ユーザー名は既に使用されています");
            return "login";
        } catch (Exception e) {
            model.addAttribute("error", "登録処理中にエラーが発生しました");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}

