package com.tour.chatbot;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.locks.Lock;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatBotInterceptor implements HandlerInterceptor {

}
