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

    private final ChatBotLock chatBotLock;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        Optional<Cookie> askToken = Arrays.stream(request.getCookies()).filter(cookie -> cookie.getName().equals("ask_token"))
                .findAny();
        Lock lock = chatBotLock.getLock(askToken.get().getValue());
        boolean locked = lock.tryLock();
        if(locked == false){
            log.info("잠겨져 있음");
            return false;
        }
        log.info("잠김");
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        Optional<Cookie> askToken = Arrays.stream(request.getCookies()).filter(cookie -> cookie.getName().equals("ask_token"))
                .findAny();
        chatBotLock.releaseLock(askToken.get().getValue());
        log.info("잠김 해제");
    }
}
