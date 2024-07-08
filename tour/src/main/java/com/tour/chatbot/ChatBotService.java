package com.tour.chatbot;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeAsyncClient;
import software.amazon.awssdk.services.bedrockruntime.model.*;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatBotService {
    private final BedrockRuntimeAsyncClient bedrockRuntimeAsyncClient;
    private final String MODEL_ID = "anthropic.claude-3-sonnet-20240229-v1:0";

    @Async()
    public CompletableFuture<String> chatWithBedrock(String prompt) {

        log.info("prompt = {}", prompt);


        try {
            Message message = Message.builder()
                    .content(ContentBlock.fromText(prompt))
                    .role(ConversationRole.USER)
                    .build();

            ConverseRequest request = ConverseRequest.builder()
                    .modelId(MODEL_ID)
                    .messages(message)
                    .inferenceConfig(InferenceConfiguration.builder()
                            .maxTokens(512)
                            .temperature(0.5F)
                            .topP(0.9F)
                            .build())
                    .build();

            CompletableFuture<ConverseResponse> futureResponse = bedrockRuntimeAsyncClient.converse(request);

            return futureResponse.thenApply(response -> {
                String responseText = response.output().message().content().get(0).text();
                return responseText;
            }).exceptionally(ex -> {
                ex.printStackTrace();
                return null;
            });

        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.failedFuture(e);
        }
    }

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

//        @Async
//    public CompletableFuture<String> chatWithBedrock(String prompt) throws InterruptedException {
//        log.info("{}",prompt);
//        try {
//            Thread.sleep(5000); // 명확한 확인을 위해 5초 sleep을 걸었다.
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        String a = "답변";
//        log.info(a);
//        return CompletableFuture.completedFuture(a);
//    }
}
