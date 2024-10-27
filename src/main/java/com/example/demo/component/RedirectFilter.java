package com.example.demo.component;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RedirectFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        //Чтобы исключить редирект анонимных юзеров мы берём строку
        if (auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String)) {
            String path = request.getRequestURI();
            if ("/login".equals(path) || "/registration".equals(path)) {
                response.sendRedirect("/");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
