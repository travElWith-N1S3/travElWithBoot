package com.tour.chatbot;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.ContentBlock;
import software.amazon.awssdk.services.bedrockruntime.model.ConversationRole;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseResponse;
import software.amazon.awssdk.services.bedrockruntime.model.Message;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatBotService {
    private final BedrockRuntimeClient bedrockClient;
    private final String MODEL_ID = "anthropic.claude-3-sonnet-20240229-v1:0";

    public String chatWithBedrock(String prompt) {
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
        return responseText;
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

}
