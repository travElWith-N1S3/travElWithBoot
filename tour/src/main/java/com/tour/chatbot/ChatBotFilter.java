package com.tour.chatbot;

import jakarta.servlet.*;

import java.io.IOException;

public class ChatBotFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        System.out.println("aaaaaa");
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
