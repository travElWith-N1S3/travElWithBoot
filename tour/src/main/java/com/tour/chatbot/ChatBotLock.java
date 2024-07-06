package com.tour.chatbot;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class ChatBotLock {
    private final ConcurrentHashMap<String, Lock> CHATBOT_LOCKMAP = new ConcurrentHashMap<>();

    public Lock getLock(String cookie){
        return CHATBOT_LOCKMAP.computeIfAbsent(cookie, (key) -> new ReentrantLock());
    }

    public void releaseLock(String cookie){
        Lock lock = CHATBOT_LOCKMAP.get(cookie);
        if(lock != null && lock.tryLock()){
            lock.unlock();
            CHATBOT_LOCKMAP.remove(cookie, lock);
        }
    }
}
