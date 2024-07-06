package com.tour.chatbot;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.ContentBlock;
import software.amazon.awssdk.services.bedrockruntime.model.ConversationRole;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseResponse;
import software.amazon.awssdk.services.bedrockruntime.model.Message;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatBotService {
    private final BedrockRuntimeClient bedrockClient;
    private final ChatBotLock chatBotLock;
    private final String MODEL_ID = "anthropic.claude-3-sonnet-20240229-v1:0";

    @Async
    public CompletableFuture<String> chatWithBedrock(String prompt) throws InterruptedException {
        log.info("prompt = {}", prompt);

        Message message = Message.builder()
                .content(ContentBlock.fromText(prompt))
                .role(ConversationRole.USER)
                .build();

        ConverseResponse response = bedrockClient.converse(request -> request
                .modelId(MODEL_ID)
                .messages(message)
                .inferenceConfig(config -> config
                        .maxTokens(512)
                        .temperature(0.5F)
                        .topP(0.9F)));

        String responseText = response.output().message().content().get(0).text();
        return CompletableFuture.completedFuture(responseText);
    }

//    @Async
//    public CompletableFuture<String> chatWithBedrock(String prompt) throws InterruptedException {
//        log.info("dd");
//        try {
//            Thread.sleep(5000); // 명확한 확인을 위해 5초 sleep을 걸었다.
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        String a = "답변";
////        log.info(a);
//        return CompletableFuture.completedFuture(a);
//    }

    public boolean validateCookie(Cookie cookie, Integer StorageValue) {
        try {
            UUID uuid = UUID.fromString(cookie.getValue());
            if (StorageValue == null) {
                log.info("저장되지 않은 쿠키");
                return false;
            }
        } catch (IllegalArgumentException e) {
            log.error("유효하지 않은 쿠키");
            return false;
        }
        return true;
    }

}
