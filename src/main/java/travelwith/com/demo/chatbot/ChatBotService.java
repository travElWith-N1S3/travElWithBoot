package travelwith.com.demo.chatbot;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeAsyncClient;
import software.amazon.awssdk.services.bedrockruntime.model.*;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatBotService {
    private String apiGatewayUrl = "https://wrrvutyink.execute-api.us-west-2.amazonaws.com/dev/prompt";

    @Async()
    public CompletableFuture<String> chatWithBedrock(String token, String prompt) {

        log.info("prompt = {}", prompt);

        try {
            ChatBotJson chatBotJson = new ChatBotJson(token, prompt);
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(chatBotJson);

            DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(apiGatewayUrl);
            factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

            WebClient client = WebClient.builder()
                    .baseUrl(apiGatewayUrl)
                    .build();

            CompletableFuture<String> stringMono = client.post()
                    .body(BodyInserters.fromValue(json))
                    .retrieve()
                    .bodyToMono(String.class).toFuture();
            return stringMono.thenApply(response -> {
                return response;
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

    public void setTokenCookie(HttpServletResponse response, UUID uuid) {
        Cookie token = new Cookie("ask_token", uuid.toString());
        token.setMaxAge(43200);  // 12시간
        response.addCookie(token);
    }
}
