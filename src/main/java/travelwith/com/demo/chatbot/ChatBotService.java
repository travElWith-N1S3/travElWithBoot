package travelwith.com.demo.chatbot;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatBotService {
    private final RedisTemplate<String, ConversationLog> redisTemplate;
    private final RedisTemplate<String, String> redisStrTemplate;
    private final Gson gson;

    private final AmazonSQS amazonSQS;

    @Value("${bedrock.aws_access_key_id}")
    private String accessKey;

    @Value("${bedrock.aws_secret_access_key}")
    private String secretKey;
    private String apiGatewayUrl = "https://wrrvutyink.execute-api.us-west-2.amazonaws.com/dev/prompt";
    private String queueName = "test-queue";

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

    public void saveNewCookie(String cookie) {
        redisStrTemplate.opsForZSet().add("cookies", cookie, 0);
        redisStrTemplate.expire(cookie, 12, TimeUnit.HOURS);
    }

    public Double getAskCount(String coookie) {
        return redisStrTemplate.opsForZSet().score("cookies", coookie);
    }

    public void increaseAskCount(String cookie) {
        redisStrTemplate.opsForZSet().incrementScore("cookies", cookie, 1);
    }

    public void saveConversation(String key, ConversationLog conversation) {
        ListOperations<String, ConversationLog> listOps = redisTemplate.opsForList();
        listOps.leftPush(key, conversation);  // 최신 대화를 왼쪽으로 추가
        listOps.trim(key, 0, 9);  // 리스트의 길이를 최대 10개로 유지
        redisTemplate.expire(key, 12, TimeUnit.HOURS);
    }

    public String getConversations(String key) {
        ListOperations<String, ConversationLog> listOps = redisTemplate.opsForList();
        return gson.toJson(listOps.range(key, 0, -1));
    }

    public boolean validateCookie(Cookie cookie, Double StorageValue) {
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

    public String pullingSQSMessage(String cookieId) {

        String queueUrl = getOrCreateQueueUrl(amazonSQS, queueName);
        String answer ="";

        // Receive messages from the queue
        ReceiveMessageRequest receiveRequest = new ReceiveMessageRequest(queueUrl)
                .withWaitTimeSeconds(5) // Long polling for messages (10 seconds)
                .withMaxNumberOfMessages(10) // Max messages to receive in one request
                .withMessageAttributeNames("All");

        ReceiveMessageResult receiveMessageResult = amazonSQS.receiveMessage(receiveRequest);
        List<Message> messages = receiveMessageResult.getMessages();

        for (Message message : messages) {
            Map<String, MessageAttributeValue> messageAttributes = message.getMessageAttributes();
            JsonParser jsonParser = new JsonParser();
            JsonObject object = (JsonObject) jsonParser.parse(message.getBody());
            JsonObject body = (JsonObject) object.get("body");
            System.out.println(body);
            System.out.println(body.get("s_id"));


//            String messageId= messageAttributes.get("cookie_id").getStringValue();
//            Gson body = new Gson();
//            body.toJson(message.getBody());
//            System.out.println(body.);

//            if(messageId == null){
//                continue;
//            }
//
//            if(messageId.equals(cookieId)){
//                answer = message.getBody();
//                System.out.println(answer);
//                DeleteMessageRequest deleteRequest = new DeleteMessageRequest(queueUrl, message.getReceiptHandle());
//                amazonSQS.deleteMessage(deleteRequest);
//                break;
//            }
        }
        return answer;
    }

    private static String getOrCreateQueueUrl(AmazonSQS sqsClient, String queueName) {
        ListQueuesResult listQueuesResult = sqsClient.listQueues(queueName);
        if (!listQueuesResult.getQueueUrls().isEmpty()) {
            return listQueuesResult.getQueueUrls().get(0);
        } else {
            CreateQueueRequest createRequest = new CreateQueueRequest(queueName);
            return sqsClient.createQueue(createRequest).getQueueUrl();
        }
    }
}
