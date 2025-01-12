package com.example.bookshelf.config;

import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class AuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        
        // ログインページとリソースは除外
        if (path.endsWith("/login") || path.endsWith("/register") || 
            path.startsWith("/css/") || path.startsWith("/js/") ||
            path.startsWith("/api/")) {
            return true;
        }

        HttpSession session = request.getSession();
        if (session.getAttribute("userId") == null) {
            response.sendRedirect("/login");
            return false;
        }

        return true;
    }
}
