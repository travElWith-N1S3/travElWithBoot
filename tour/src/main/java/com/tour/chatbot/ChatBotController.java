package com.tour.chatbot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.ContentBlock;
import software.amazon.awssdk.services.bedrockruntime.model.ConversationRole;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseResponse;
import software.amazon.awssdk.services.bedrockruntime.model.Message;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/v1")
public class ChatBotController {

    private final BedrockRuntimeClient bedrockClient;
    private final String MODEL_ID="anthropic.claude-3-sonnet-20240229-v1:0";

    @GetMapping("/chatbot")
    public String chatWithBedrock(@RequestParam("prompt") String prompt){
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
        log.info("response = {}", responseText);

        return responseText;
    }

}
