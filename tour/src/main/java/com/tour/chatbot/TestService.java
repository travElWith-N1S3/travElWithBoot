package com.tour.chatbot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class TestService {

    @Async
    public CompletableFuture<String> getTest() {
        log.info("sss");
        try {
            Thread.sleep(5000); // 명확한 확인을 위해 5초 sleep을 걸었다.
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String a = "답변";
        log.info(a);
        return CompletableFuture.completedFuture(a);
    }
}
