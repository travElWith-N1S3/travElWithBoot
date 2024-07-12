package travelwith.com.demo.chatbot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatBotService {
    private final RedisTemplate<String, ConversationLog> redisTemplate;
    private final Gson gson;

    private String apiGatewayUrl = "https://wrrvutyink.execute-api.us-west-2.amazonaws.com/dev/prompt";

    @Async()
    public CompletableFuture<String> chatWithBedrock(String token, ChatBotPrompt prompt) {
        log.info("prompt = {}", prompt.getPrompt());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ChatBotJson chatBotJson = new ChatBotJson(token, objectMapper.writeValueAsString(prompt));
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

//    public void saveNewCookie(String cookie){
//        redisTemplate.opsForValue().setIfAbsent(cookie, 0);
//        redisTemplate.expire(cookie, 12, TimeUnit.HOURS);
//    }

//    public int getAskCount(String coookie){
//        return (int) redisTemplate.opsForValue().get(coookie);
//    }

//    public void increaseAskCount(String cookie){
//        redisTemplate.opsForValue().increment(cookie);
//    }


//    public void saveConversation(String key, ConversationLog conversation) {
//        ListOperations<String, Object> listOps = redisTemplate.opsForList();
//        listOps.leftPush(key, conversation);  // 최신 대화를 왼쪽으로 추가
//        listOps.trim(key, 0, 9);  // 리스트의 길이를 최대 10개로 유지
//        redisTemplate.expire(key, 12, TimeUnit.HOURS);
//    }

    public void saveConversation(String key, ConversationLog conversation) {
        ListOperations<String, ConversationLog> listOps = redisTemplate.opsForList();
        listOps.leftPush(key, conversation);  // 최신 대화를 왼쪽으로 추가
        listOps.trim(key, 0, 9);  // 리스트의 길이를 최대 10개로 유지
        redisTemplate.expire(key, 12, TimeUnit.HOURS);
    }

//    public String getConversations(String key) {
//        ListOperations<String, Object> listOps = redisTemplate.opsForList();
//        return gson.toJson(listOps.range(key, 0, -1));
//    }

    public String getConversations(String key) {
        ListOperations<String, ConversationLog> listOps = redisTemplate.opsForList();
        return gson.toJson(listOps.range(key, 0, -1));
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
